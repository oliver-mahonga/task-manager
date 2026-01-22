🚀 Task Master EliteA sleek, high-performance Java Swing desktop application for task management, featuring a local SQLite database and a modern "Midnight" dark-mode interface.✨ Features🌑 Elite Dark Mode: A professional midnight-themed UI designed for focus.📊 Real-time Statistics: Instant tracking of pending vs. completed tasks at the bottom of the app.🔍 Live Search: Filter through your tasks instantly as you type.✅ Status Toggling: Single-click logic to switch tasks between Pending and Completed.🗄️ Persistence: All tasks are saved locally in a tasks.db file using SQLite.🛡️ Safety First: Confirmation prompts for destructive actions like "Clear All."🛠️ Installation & Setup1. PrerequisitesEnsure you have the Java Development Kit (JDK) installed ($11$ or higher recommended).2. Project StructureYour folder should look like this:PlaintextMyJavaApp/
├── lib/
│   ├── sqlite-jdbc.jar
│   ├── slf4j-api.jar
│   └── slf4j-nop.jar
├── src/
│   └── TaskManagerApp.java
└── README.md
3. Build InstructionsOpen your terminal in the project root and run the following commands:Bash# Create build directory
mkdir -p build

# Extract libraries into build
cd build
jar -xf ../lib/sqlite-jdbc.jar
jar -xf ../lib/slf4j-api.jar
jar -xf ../lib/slf4j-nop.jar
cd ..

# Compile the source code
javac -cp "lib/*" -d build src/TaskManagerApp.java

# Package into an executable Fat JAR
jar cvfe TaskMaster.jar TaskManagerApp -C build .
🚀 How to RunOnce you have built the JAR file, simply run:Bashjava -jar TaskMaster.jar
🎮 How to UseAdd a Task: Type your task in the input field at the bottom and click Add Task.Complete a Task: Select a row in the table and click Check/Uncheck. The status will turn green!Search: Use the top-right search bar to find specific tasks instantly.Delete: Select a task and hit Delete, or use Clear All to start fresh.🔧 Technologies UsedLanguage: Java (Swing API)Database: SQLite (via JDBC)Logging: SLF4J (Nop implementation)Build Tool: Manual JAR manifest packaging📜 LicenseThis project is open-source and free to use.






## RUN COMMAND

javac -cp "lib/*" -d build src/TaskManagerApp.java
jar cvfe TaskMaster.jar TaskManagerApp -C build .
java -jar TaskMaster.jar
