# Task Manager API

## Overview
  Task Manager is a powerful RESTful API that enables developers to build robust task management applications. Built using the Spring Framework, the API provides a wide range of functionalities that allow developers to create user registration and login systems, reset passwords, and manage tasks.  \
  It also features team management functionalities that allow developers to create teams, join existing ones. It helps users collaborate with other members of team.

## Technologies Used
- Java 17
- REST API
- Spring Boot 3.0.2
- Spring Security
- JWT Tokens
- Maven
- Hibernate
- Lombok
- JUnit
- Mockito

## Functionality
### User Registration and Login
The authentication is done using JWT tokens, which ensures secure communication between the client and server.
- Users can register on the Task Manager application by providing their first name, last name, email and password. 
- After successful registration, users can log in using their credentials.

### Reset Password
- Users can reset their password if they forgot it. The application will send an email with a link to reset the password.

### Task Management
Once a user is logged in, they can perform the following actions related to task management:
- Create a new task: Users can create a new task by providing a task name, description, and deadline. If the task is not completed by the deadline, the progress of the task will automatically change to "OVERDUE".
- Edit a task: Users can edit a task by updating its name, description, and deadline.
- Delete a task: Users can delete a task. When a task is deleted, the progress of the task will be set to "CLOSED".
- Change progress: Users can change the progress of a task to "NOT STARTED", "IN PROGRESS", or "COMPLETED".
- List all tasks: Users can list all the tasks they have created and sort them by deadline, progress, or name.

### Team Management
- Users can create a team or join an existing team on the Task Manager application. Once a team is created, an invite code with a length of 6 symbols will be generated. 
- Users can join the team by using the invite code or sending a join request if the team is set to private. The owner or admin of the team can accept or decline requests, and also add admins to the team. 
- Admins have the same functionality as the owner but can't remove or edit other admins. 
- In a team, members can't create or edit tasks by themselves, it can be done only by the admin or owner. Members can only change the progress of a task.

### Support Team
- There is a support team that can help users with tasks by watching over them, deleting them, editing them, and performing other tasks if necessary.

## Installation
To run the Task Manager application, follow these steps:
1. Clone the repository to your local machine using `git clone https://github.com/sashahv/task-manager.git`.
2. Navigate to the project directory using `cd task-manager`.
3. Install the required dependencies using `mvn install`.
4. Start the application using `mvn spring-boot:run`.
5. The application will be available at `http://localhost:8080`.

## Contact Information
If you have any questions or issues with the project, please feel free to reach out to the developer at s.hvozditskyi@gmail.com.
