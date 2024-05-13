# Operating System Simulator
A simulation of the mechanics of an operating system, including cooperative multi-tasking, process scheduling, priority management, devices, messaging, memory management, and more. This was built as an educational project to help derive a deeper understanding of how operating systems work.

## Usage
To run the simulator, download the files by clicking the green "Code" button and clicking "Download ZIP". Then, open your IDE of choice - for reference, I used IntelliJ, although VS Code and others should work - and create a new project. Either drop the files from the ZIP directly into your project or import them from the IDE. 

>Note: The simulator should run on most modern versions of Java, though it was built on JDK 17.

Now you should be able to run the operating system. Simply click the run button and you should see terminal output:

![Screenshot 2024-05-13 165921](https://github.com/TristanPeloquin/OS-Simulator/assets/98565896/2b7362ee-e862-4a39-8281-f09eae823ab6)

You can uncomment the test classes for quick experimentation if you'd like. For example, if you want to test message sending and waiting, you can do something like this: 

![image](https://github.com/TristanPeloquin/OS-Simulator/assets/98565896/437e625c-4bfa-4c15-b7e7-746ffdc06d62)

If you'd like to create your own processes, the code is extensively commented/documented. Look at the OS class for the types of calls you can use and at the test classes for what kind of format works best.
