import java.util.Scanner;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class Lab3 {

	public static void main(String[] args) throws FileNotFoundException{
	
	
		Scanner input = new Scanner(args[0]);
		FileReader fileread = new FileReader(input.next());
		Scanner scan = new Scanner(fileread);
		
		FIFOrm fifo = new FIFOrm();//create an instance of FIFO resource manager
		
		Banker banker = new Banker();//create an instance of BANKER resource manager
		
		String line1 = scan.nextLine();
		String firstline[] = line1.split("\\s+");
		
		int numoftasks = Integer.parseInt(firstline[0]);
		int numofrcs = Integer.parseInt(firstline[1]);

		fifo.initialize(numoftasks, numofrcs);
		banker.initialize(numoftasks, numofrcs);
		
		//create each task
		Task[] tasks = new Task[numoftasks];
		Task[] btasks = new Task[numoftasks];
		for(int t = 0; t < numoftasks; t++) {
			int taskid = t + 1;
			tasks[t] = new Task(taskid);
			btasks[t] = new Task(taskid);
			//System.out.println("Task " + tasks[t].taskID);
		}
		
		//build array that keeps track of each resource's available units
		int afterTRindex = 2;
		for(int i = 1; i < firstline.length-1; i++) {
			int available_units = Integer.parseInt(firstline[afterTRindex]);
			fifo.available[i-1] = available_units;
			banker.available[i-1] = available_units;
			//at each index of array available, unit of resource type i is stored
			//System.out.println("resource type " + i + " has " + fifo.available[i-1] + " available units");
			afterTRindex += 1;
		}
		
		//put all activities for each task into an array
		//each task has its own unique array of activities
		while(scan.hasNext()) {
			String activity = scan.nextLine();
			if(!activity.contentEquals("")) {//account for any blank lines
				//System.out.println("activity: " + activity);
				String[] content = activity.split("\\s+");
				String action = content[0];
				int tasknumber = Integer.parseInt(content[1]);
				tasks[tasknumber-1].Activities.add(activity);
				btasks[tasknumber-1].Activities.add(activity);
				//task 1 is at index 0 of array "tasks" --> add the String activity to the task's array of activities
				//(so, task i is at index i-1 of array "tasks")
			}
		}
	
		System.out.println();
		//System.out.println("-------------------FIFO-----------------------");
		fifo.execute(tasks, numofrcs);
		//fifo rm should take an array of tasks, each task having their own set of activities
		System.out.println();
		System.out.println();
		//System.out.println("-------------------BANK-----------------------");
		banker.execute(btasks, numofrcs);
		
		input.close();
		scan.close();
	}//end of main class

}//end of Lab3 class


