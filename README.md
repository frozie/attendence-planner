# Problem
In a DevOps-Project members want to keep the production system running under watch of a rotating duty of oncall/onsite available colleagues.
OpsGenie is good tool to maintain and act on incidents, but it lacks proper planning of schedules. What we need is a tool where employees 
* can create NonAvailabilityNotices (a time span in which they are not available for a shift)
* are not scheduled for escalation and normal schedule at the same time
* have time to rest between shifts
* have other teams on state/company holidays
* are scheduled still *somehow fair* (or according to their wishes to participate in schedules)

# Solution/Vision
The **oncall-schedule-planner** consists of:
* a frontend and backend to manage NonAvailabilityNotices
* a frontend and backend to create a schedule for a specific time frame (for example the next month)
* a optimizer leveraging [Google's OR-Tools](https://developers.google.com/optimization)
* a postgres database to store NonAvailabilityNotices and other configuration/meta data
* a adapter for OpsGenie to create overrides

As a way to think about the optimization problem you can start with the python notebook https://github.com/frozie/oncall-schedule-planner/blob/master/exploration/Exploration.ipynb

*This is not a production ready tool yet, but you can help to make it work for you and for others by contributing.*

# Quick Start
* clone the repo
* download the OR-Tools use [version 6.10](https://github.com/google/or-tools/releases/tag/v6.10). Then extract the lib folder and move the contents to optimization/lib.
* setup and configure a database (maybe use docker). Basic example data is: https://github.com/frozie/oncall-schedule-planner/blob/master/src/main/resources/data.sql
* run ./gradlew bootRun
* use the browser to navigate on the main page
