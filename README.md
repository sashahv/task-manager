
#Task Manager
##Overview
Task Manager is a web application that helps users to manage their tasks and collaborate with others by creating and joining teams. The application is built using Spring Framework and it allows users to perform various actions such as registration, login, reset password, and manage tasks.

###Functionality
##User Registration and Login
Users can register on the Task Manager application by providing their email, password, and username. After successful registration, users can log in using their credentials. The authentication is done using JWT tokens, which ensures secure communication between the client and server.

##Reset Password
Users can reset their password if they forget it by clicking on the "Forgot Password" link. The application will send an email with a link to reset the password.

##Task Management
Once a user is logged in, they can perform the following actions related to task management:
-Create a new task: Users can create a new task by providing a task name, description, and deadline. If the task is not completed by the deadline, the progress of the task will automatically change to "OVERDUE".
-Edit a task: Users can edit a task by updating its name, description, and deadline.
-Delete a task: Users can delete a task. When a task is deleted, the progress of the task will be set to "CLOSED".
-Change progress: Users can change the progress of a task to "NOT STARTED", "IN PROGRESS", or "COMPLETED".
-List all tasks: Users can list all the tasks they have created and sort them by deadline, progress, or name.

##Team Management
Users can create a team or join an existing team on the Task Manager application. Once a team is created, an invite code with a length of 6 symbols will be generated. Users can join the team by using the invite code or sending a join request if the team is set to private. The owner of the team can accept or decline requests, and also add admins to the team. Admins have the same functionality as the owner but can't remove or edit other admins. In a team, members can't create or edit tasks by themselves, it can be done only by the admin or owner. Members can only change the progress of a task.

##Support Team
There is a support team that can help users with tasks by watching over them, deleting them, editing them, and performing other tasks if necessary.

##Installation
To run the Task Manager application, follow these steps:
-Clone the repository to your local machine using git clone https://github.com/your-username/task-manager.git.
-Navigate to the project directory using 'cd task-manager'.
-Install the required dependencies 'using mvn install'.
-Start the application using 'mvn spring-boot:run'.
-The application will be available at 'http://localhost:8080'.

##Credits
If you have any questions or issues with the project, please feel free to reach out to the developer at s.hvozditskyi@gmail.com.
