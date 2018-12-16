package ondutyScheduler.optimizer

import com.google.ortools.sat.CpModel
import com.google.ortools.sat.CpSolver
import com.google.ortools.sat.IntVar
import ondutyScheduler.util.intersectingTimes
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max

/**
 * @author Frowin Ziegler, MaibornWolff GmbH
 */

class TimeSlot(
        val starting: LocalDateTime,
        val ending: LocalDateTime,
        val isPayed: Boolean = true //TODO a bit ugly here in TimeSlot
) {
    fun intersects(other: TimeSlot) = intersectingTimes(starting, ending, other.starting, other.ending)
}

data class Employee(
        val targetWorkloadMinNormal: Int,
        val targetWorkloadMaxNormal: Int,
        val targetWorkloadMinEscalation: Int,
        val targetWorkloadMaxEscalation: Int,
        val nonAvailableTimeSlotsNormal: Set<TimeSlot>,
        val nonAvailableTimeSlotsEscalation: Set<TimeSlot>,
        val identifier: Any
) {
    companion object {
        fun targetWorkloadMinNormalExtractor(e: Employee) = e.targetWorkloadMinNormal.toLong()
        fun targetWorkloadMaxNormalExtractor(e: Employee) = e.targetWorkloadMaxNormal.toLong()
        fun targetWorkloadMinEscalationExtractor(e: Employee) = e.targetWorkloadMinEscalation.toLong()
        fun targetWorkloadMaxEscalationExtractor(e: Employee) = e.targetWorkloadMaxEscalation.toLong()
    }
}

data class OptimizerInput(
        val normalEmployees: Set<Employee>,
        val escalationEmployees: Set<Employee>,
        val normalTimeSlots: List<TimeSlot>,
        val escalationTimeSlots: List<TimeSlot>
)

data class MappedEmployeeToTimeSlot(
        val timeSlot: TimeSlot,
        val employee: Employee
)

data class OptimizerOutput(
        val normalSchedule: List<MappedEmployeeToTimeSlot>,
        val escalationSchedule: List<MappedEmployeeToTimeSlot>
)

class SatSum(val elements: Array<IntVar>) {
    fun toIntVar(model: CpModel, bounds: Long): IntVar {
        val result = model.newIntVar(-bounds, bounds, "sum")
        model.addLinearSumEqual(elements, result)
        return result
    }
}

data class Cost(
        val actualCost: IntVar,
        val maximalCost: Int
)

@Service
class OptimizerService {

    private val BOUNDS = 10000L;

    fun calculateSolution(input: OptimizerInput): OptimizerOutput {
        System.loadLibrary("jniortools")
        //val solver = Solver("scheduleOptimization")
        val model = CpModel()

        val assignedNormalTimeSlots = makeAssignmentModel(model, input.normalEmployees, input.normalTimeSlots)
        val assignedEscalationTimeSlots = makeAssignmentModel(model, input.escalationEmployees, input.escalationTimeSlots)

        addConstraintsNonAvailability(model, assignedNormalTimeSlots, input.normalTimeSlots, input.normalEmployees, true)
        addConstraintsNonAvailability(model, assignedNormalTimeSlots, input.normalTimeSlots, input.normalEmployees, false)
        addConstraintEachTimeSlotIsAssignedExactlyToOneEmployee(model, assignedNormalTimeSlots)
        addConstraintEachTimeSlotIsAssignedExactlyToOneEmployee(model, assignedEscalationTimeSlots)
        addConstraintEveryoneNeedsAnEscalation(model, assignedNormalTimeSlots, assignedEscalationTimeSlots, input.normalEmployees, input.escalationEmployees)
        addConstraintNoConsecutiveShifts(model, assignedNormalTimeSlots, input.normalTimeSlots, input.normalEmployees)

        val costEscalationRotations = costRotations(model, assignedNormalTimeSlots, input.escalationTimeSlots, input.escalationEmployees)
        val costNormalRotations = costRotations(model, assignedNormalTimeSlots, input.normalTimeSlots, input.normalEmployees)
        val costEscalationTargetWorkload = costTargetWorkload(model, assignedEscalationTimeSlots, input.escalationTimeSlots, input.escalationEmployees, Employee.Companion::targetWorkloadMaxEscalationExtractor, Employee.Companion::targetWorkloadMinEscalationExtractor)
        val costNormalTargetWorkload = costTargetWorkload(model, assignedNormalTimeSlots, input.normalTimeSlots, input.normalEmployees, Employee.Companion::targetWorkloadMaxNormalExtractor, Employee.Companion::targetWorkloadMinNormalExtractor)


        val actualCostVector = listOf(
                costEscalationRotations.actualCost,
                costNormalRotations.actualCost,
                costEscalationTargetWorkload.actualCost,
                costNormalTargetWorkload.actualCost)
        val factors = arrayOf(
                1,
                costEscalationRotations.maximalCost,
                costEscalationRotations.maximalCost * costNormalRotations.maximalCost,
                costEscalationRotations.maximalCost * costNormalRotations.maximalCost * costEscalationTargetWorkload.maximalCost)

        val optimizeProduct = model.newIntVar(0, BOUNDS, "toOptimize")
        model.addScalProdEqual(actualCostVector.toTypedArray(), factors.toIntArray(), optimizeProduct)

        model.minimize(optimizeProduct)

        val solver = CpSolver()
        val solvedStatus = solver.solve(model)
        println(solvedStatus.name)
        return OptimizerOutput(
                readSolutionForAssignment(solver, assignedNormalTimeSlots),
                readSolutionForAssignment(solver, assignedEscalationTimeSlots))
    }

    private fun makeAssignmentModel(model: CpModel, employees: Collection<Employee>, timeSlots: Collection<TimeSlot>): HashMap<MappedEmployeeToTimeSlot, IntVar> {
        val assignedTimeSlots = HashMap<MappedEmployeeToTimeSlot, IntVar>()
        for (employee in employees) {
            for (timeSlot in timeSlots) {
                val assignedVar = model.newBoolVar("${timeSlot.starting} is for ${employee.identifier}")
                assignedTimeSlots[MappedEmployeeToTimeSlot(timeSlot, employee)] = assignedVar
            }
        }
        return assignedTimeSlots
    }

    private fun workload(assignmentModel: Map<MappedEmployeeToTimeSlot, IntVar>, timeSlots: Collection<TimeSlot>, employee: Employee): SatSum {
        val allTimeSlotsForEmployee = timeSlots.map { assignmentModel[MappedEmployeeToTimeSlot(it, employee)] }
        return SatSum(allTimeSlotsForEmployee.filterNotNull().toTypedArray())
    }

    private fun minusAbs(model: CpModel, sum: SatSum, constant: Long): IntVar {
        val satSumIntVar = model.newIntVar(-BOUNDS, BOUNDS, "absm1")
        val constantIntVar = model.newConstant(-constant)
        val absValueIntVar = model.newIntVar(-BOUNDS, BOUNDS, "abs")
        model.addLinearSumEqual(sum.elements.plus(constantIntVar), satSumIntVar)
        model.addAbsEquality(absValueIntVar, satSumIntVar)
        return absValueIntVar
    }

    private fun addConstraintsNonAvailability(model: CpModel, assignmentModel: Map<MappedEmployeeToTimeSlot, IntVar>, timeSlots: Collection<TimeSlot>, employees: Collection<Employee>, forNormalEmployees: Boolean) {
        for (employee in employees) {
            val nonAvailableTimeSlots = if (forNormalEmployees) {
                employee.nonAvailableTimeSlotsNormal
            } else {
                employee.nonAvailableTimeSlotsEscalation
            }
            for (timeSlot in timeSlots) {
                for (nonAvailableTimeSlot in nonAvailableTimeSlots) {
                    if (timeSlot.intersects(nonAvailableTimeSlot)) {
                        val solverVar = assignmentModel[MappedEmployeeToTimeSlot(timeSlot, employee)]
                        model.addEquality(solverVar, 0)
                        println("make nonavailability: " + MappedEmployeeToTimeSlot(timeSlot, employee))
                    }
                }
            }
        }
    }

    private fun addConstraintEachTimeSlotIsAssignedExactlyToOneEmployee(model: CpModel, assignmentModel: Map<MappedEmployeeToTimeSlot, IntVar>) {
        val groupByTimeSlots = assignmentModel.keys.groupBy { it.timeSlot }
        for (mappedTimeSlots in groupByTimeSlots.values) {
            val allEmployeesFotTimeSlot = mappedTimeSlots.map { assignmentModel[it] }
            model.addLinearSumEqual(allEmployeesFotTimeSlot.toTypedArray(), 1)
        }
    }

    private fun addConstraintEveryoneNeedsAnEscalation(model: CpModel, assignedNormalTimeSlots: Map<MappedEmployeeToTimeSlot, IntVar>, assignedEscalationTimeSlots: Map<MappedEmployeeToTimeSlot, IntVar>, normalEmployees: Collection<Employee>, escalationEmployees: Collection<Employee>) {
        val escalationAndNormalEmployees = normalEmployees.intersect(escalationEmployees)
        for ((mappedNormalTimeSlot, modelVarNormal) in assignedNormalTimeSlots.filter { it.key.employee in escalationAndNormalEmployees }) {
            for ((mappedEscalationTimeSlot, modelVarEscalation) in assignedEscalationTimeSlots.filter { it.key.employee == mappedNormalTimeSlot.employee }) {
                if (mappedNormalTimeSlot.timeSlot.intersects(mappedEscalationTimeSlot.timeSlot)) {
                    model.addImplication(modelVarNormal, modelVarEscalation.not())
                }
            }
        }
    }

    private fun addConstraintNoConsecutiveShifts(model: CpModel, assignmentModel: Map<MappedEmployeeToTimeSlot, IntVar>, timeSlots: List<TimeSlot>, employees: Collection<Employee>) {
        for (employee in employees) {
            var lastTimeSlot = timeSlots.first()
            for (timeSlot in timeSlots.drop(1)) {
                model.addImplication(assignmentModel[MappedEmployeeToTimeSlot(lastTimeSlot, employee)], assignmentModel[MappedEmployeeToTimeSlot(timeSlot, employee)]?.not())
                lastTimeSlot = timeSlot
            }
        }
    }

    private fun costRotations(model: CpModel, assignmentModel: Map<MappedEmployeeToTimeSlot, IntVar>, timeSlots: List<TimeSlot>, employees: Collection<Employee>): Cost {
        val rotations = timeSlots.chunked(employees.size)
        val totalCostFactors = LinkedList<IntVar>()
        for (rotation in rotations) {
            for (employee in employees) {
                val workload = workload(assignmentModel, rotation, employee)
                val cost = minusAbs(model, workload, 1)
                totalCostFactors.add(cost)
            }
        }
        val maxCost = rotations.size * 2 * (employees.size - 1)
        return Cost(SatSum(totalCostFactors.toTypedArray()).toIntVar(model, BOUNDS), maxCost)
    }

    private fun costTargetWorkload(model: CpModel, assignmentModel: Map<MappedEmployeeToTimeSlot, IntVar>, timeSlots: Collection<TimeSlot>, employees: Collection<Employee>, maxTargetExtractor: (Employee) -> Long, minTargetExtractor: (Employee) -> Long): Cost {
        val relevantTimeSlots = timeSlots.filter { it.isPayed }
        val allTargetMisses = ArrayList<IntVar>(employees.size)
        for (employee in employees) {
            val workload = workload(assignmentModel, relevantTimeSlots, employee)
            val targetMinMiss = minusAbs(model, workload, minTargetExtractor(employee))
            val targetMaxMiss = minusAbs(model, workload, maxTargetExtractor(employee))
            val minTargetMiss = model.newIntVar(0, max(minTargetExtractor(employee), maxTargetExtractor(employee)), "minTargetMiss")
            model.addMinEquality(minTargetMiss, arrayOf(targetMaxMiss, targetMinMiss))
            allTargetMisses.add(minTargetMiss)
        }

        val maxTargetMissOverall = model.newIntVar(0, relevantTimeSlots.size.toLong(), "maxTargetMissOverall")
        model.addMaxEquality(maxTargetMissOverall, allTargetMisses.toTypedArray())

        return Cost(maxTargetMissOverall, relevantTimeSlots.size)
    }

    private fun readSolutionForAssignment(solver: CpSolver, assignmentModel: HashMap<MappedEmployeeToTimeSlot, IntVar>): List<MappedEmployeeToTimeSlot> {
        return assignmentModel.filter { solver.booleanValue(it.value) }.map { it.key }.sortedBy { it.timeSlot.starting }
    }
}

