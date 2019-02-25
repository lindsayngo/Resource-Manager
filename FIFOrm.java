import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.HashMap;
public class FIFOrm {
	int alltasks; //total tasks
	int allrcs;	//total resources/number of resource types
	int[] available; //stores available units of resources of each resource type
	int[][] allocatedrcs; //maps each task to number of allocated resources
	
	HashMap <Task, int[]> alloc_map = new HashMap <Task, int[]> (); //key = task, value = array of resources each task is currently allocated
	
	public void initialize(int tasks, int rcs) {
		alltasks = tasks;
		allrcs = rcs;		
		
		 //index + 1 = recourse type
		available = new int [allrcs]; 
		
		 //row + 1 = task id, column + 1 = resource type, content = number of allocated resources that task has of that resource
		 //should be filled with the numbers after R in the input
		//allocatedrcs = new int [alltasks][allrcs]
	}
	
	public boolean stillDeadlock(Task[] tasks) {
		
		return false;
	}
	
	public void execute(Task[] tasks, int total_number_of_resources) {
	
		int cycle = 0;
		boolean AllFinished = false;//if AllFinished = true, all tasks are either finished or aborted, while loop can be exited
		boolean deadlock = false;
		
		ArrayList <Task> blockedtasks = new ArrayList <Task> ();
		ArrayList <Task> readytasks = new ArrayList <Task> ();
		
		int [] releasedrcs = new int [total_number_of_resources];
		//since released resources can only be available in the NEXT cycle, we must keep track of them and add them AFTER current cycle completes
		//index = resource type - 1, content = number of resources that must be added to available
		
		//available = new int [total_number_of_resources];
		allocatedrcs = new int[tasks.length][total_number_of_resources];
		
		int total_current_activities, total_blocked_tasks;//for deadlock checking
		
		for(int i = 0; i < tasks.length; i ++) {
			int[] allocrcs = new int[total_number_of_resources];//should be filled with zeros, index = resource type - 1, int = unit	
			alloc_map.put(tasks[i], allocrcs); //maps each taks to its resource types + the allocated units
		}

		
		while(AllFinished == false) {
		
			total_current_activities = 0;
			total_blocked_tasks = 0;
			readytasks.clear();
			int index = 0;
			//check for/deal with deadlock
			while(deadlock == true) {
			
				//System.out.println("The blocked tasks look like: ");
//				for(int h = 0; h < blockedtasks.size(); h++) {
//					System.out.println("Task " + blockedtasks.get(h).taskID);
//				}
				
				//System.out.println("Deadlock is true.");
				int remove = 0;
				for(int i = 0; i < tasks.length; i++) {	
					
					if(tasks[i].aborted == false && tasks[i].finished == false) {
						//remove the FIRST encountered tasks that is not aborted and not finished
						tasks[i].aborted = true;
						
						for(int j = 0; j < total_number_of_resources; j++) {
							//System.out.println("Task " + tasks[i].taskID + " is aborted.");
							int rel = alloc_map.get(tasks[i])[j];
							//System.out.println("Task " + tasks[i].taskID + " is releasing " + rel);
							
							available[j] += rel;
							int r = j + 1;
							//System.out.println("New available units of resource " + r + " is " + available[j]);
							
							alloc_map.get(tasks[i])[j] = 0; //the task no longer has resources
							index = tasks[i].taskID - 1;
							//System.out.println(index);
							for(int u = 0; u < blockedtasks.size(); u++) {
								if(blockedtasks.get(u).taskID == tasks[i].taskID) {
									remove = u;
									break;
								}
							}
							//System.out.println("Task to remove from blocked tasks: Task " + blockedtasks.get(remove).taskID);
							//blockedtasks.remove(i);
						}
						
						break;//break out of for loop
					}
						
				}
			
				blockedtasks.remove(remove);
				
				//determine if there is STILL a deadlock
				//this should not CHANGE any available or allocations; it is merely a check
				int num_satisfied = 0;
				int num_not_satisfied = 0;
				int num_tasks = 0;
				for(int o = 0; o < tasks.length; o++) {
					if(tasks[o].aborted == false && tasks[o].finished == false) {
						String taskactivity = tasks[o].Activities.get(0);
						String[] taskactivityarr = taskactivity.split("\\s+");
						int resourcetype = Integer.parseInt(taskactivityarr[2]) - 1;
						int requestedrcs = Integer.parseInt(taskactivityarr[3]);
						if(available[resourcetype] >= requestedrcs) {
							num_satisfied += 1;
						}
						else {
							num_not_satisfied += 1;
						}
						num_tasks += 1;
					}
				}
				
				if(num_not_satisfied == num_tasks) {
					//System.out.println("There is still a Deadlock.");
					//if there is still a deadlock, you must abort the next task
					//System.out.println("The blocked tasks now look like: ");
					//for(int h = 0; h < blockedtasks.size(); h++) {
						//System.out.println("Task "+ blockedtasks.get(h).taskID);
					//}
				}
				else {
					//System.out.println("No more Deadlock.");
					deadlock = false;
					//exit while loop, abort another task
				}
				
			}//end of deadlock check/update

		
		//check/update blocked prcs
			ArrayList <Task> still_blocked = new ArrayList <Task> ();
			while(blockedtasks.isEmpty() == false) {
				//System.out.println("Blocked tasks still has tasks");
				Task t = blockedtasks.remove(0);//keep removing and using the first element in the blocked task arraylist
				
				total_current_activities += 1;
				String current_act = t.Activities.get(0);
				//System.out.println("This blocked task's current activity is: " + current_act);
				String[] arr = current_act.split("\\s+");
				int req = Integer.parseInt(arr[2]) - 1;
				//System.out.println(req);
				int requnits = Integer.parseInt(arr[3]);
				//System.out.println(requnits);
				//System.out.println(available[req]);
				
				if(available[req] - requnits < 0) {
					//STILL block the task
					//System.out.println("stil must block Task " + t.taskID);
					t.status = "blocked";
					total_blocked_tasks += 1;
					t.waittime += 1;
					still_blocked.add(t);
				}
				
				else {
					//satisfy the request
					int printreq= req + 1;
					
					//System.out.println("Blocked Task " + t.taskID + "'s request can be satisfied");
					t.Activities.remove(0);
					
					alloc_map.get(t)[req] += requnits;
					//System.out.println("Task " + t.taskID + " is allocated " + alloc_map.get(t)[req]);
					
					available[req] -= requnits;
					//System.out.println("New available units of resource " + printreq + " is " + available[req]);
					
					t.status = "ready";
					
					readytasks.add(t);
				}	
				
			}
			
			blockedtasks.addAll(still_blocked);

			//System.out.println();
			//System.out.print("Cycle ");
			//System.out.print(cycle);
			//System.out.print("-");
			//System.out.print(cycle+1);
			//System.out.println();
			
			cycle += 1;
			
			//for(int i = 0; i < readytasks.size(); i++) {
				//System.out.println("Task " + readytasks.get(i).taskID + "'s pending request is granted");
			//}

			
		//for each task, deal with activities
			for(int i = 0; i < tasks.length; i++) {
				
				String activity = tasks[i].Activities.get(0);
				String[] activityarr = activity.split("\\s+");

				//if( the task is NOT currently blocked and NOT currently computing )
				//process one activity for each process per ONE cycle
				if(tasks[i].aborted == false && tasks[i].finished == false && readytasks.contains(tasks[i]) == false && blockedtasks.contains(tasks[i]) == false) {
					
					if(tasks[i].computing == 0) {
						
						if(activityarr[0].equals("initiate")) {
							tasks[i].status = "ready";
							tasks[i].Activities.remove(0); //remove the initiate activity for each task, so the top of the arraylist is now NOT an initiate activity																																																																																										
							
							//System.out.println("task " + tasks[i].taskID + " initiated");
							//System.out.println("next activity = " +tasks[i].Activities.get(0));
							
							total_current_activities += 1;
						}
						
						else if(activityarr[0].equals("request")) {
							//checking if a task request can be satisfied:
							int tasknumber = i + 1;
							int requestedrc = Integer.parseInt(activityarr[2])-1;
							
							int requestedunits = Integer.parseInt(activityarr[3]);
							//System.out.println("available of requested rc = " + available[requestedrc]);
							//System.out.println("requested units " + requestedunits);
							if(available[requestedrc] - requestedunits < 0) {
								//block the task
								//System.out.println("BLOCK Task " + tasknumber);
								tasks[i].status = "blocked";
								total_blocked_tasks += 1;
								blockedtasks.add(tasks[i]);
								tasks[i].waittime += 1;
							}
							else {
								//satisfy the request
								tasks[i].Activities.remove(0);
								
								alloc_map.get(tasks[i])[requestedrc] += requestedunits;
								//System.out.println("Task " + tasks[i].taskID + " is allocated " + alloc_map.get(tasks[i])[requestedrc]);
								tasks[i].status = "ready";
								
								available[requestedrc] = available[requestedrc] - requestedunits;
								//System.out.println("New available units of resource " + activityarr[2] + " is " + available[requestedrc]);
								
								if(tasks[i].Activities.get(0).contains("terminate")) {
									//System.out.println("next activity = terminate, so task = finished");
									tasks[i].finished = true;
									tasks[i].finishcycle = cycle;
								}
								
							}
							
							total_current_activities += 1;
							
						}
						
						else if(activityarr[0].equals("release")) {
							//any released units should only be available in the NEXT cycle
							
							int tasknumber = i;
							int releasedrc_index = Integer.parseInt(activityarr[2])-1;
							int releasedunits = Integer.parseInt(activityarr[3]);
							
							//System.out.println("Task " + tasks[i].taskID + " releases " + releasedunits + " of resource " + activityarr[2]);
							
							alloc_map.get(tasks[i])[releasedrc_index] -= releasedunits;
							//System.out.println("Task " + tasks[i].taskID + " now has " + alloc_map.get(tasks[i])[releasedrc_index]);
							
//							available[releasedrc] += releasedunits;
//							System.out.println("New available units of resource " + activityarr[2] + " is " + available[releasedrc]);
							releasedrcs[releasedrc_index] += releasedunits;
							
							tasks[i].Activities.remove(0);
							
							if(tasks[i].Activities.get(0).contains("terminate")) {
								//System.out.println("next activity = terminate, so task = finished");
								tasks[i].finished = true;
								tasks[i].finishcycle = cycle;
							}
							
							total_current_activities += 1;
							
						}
						
						else if(activityarr[0].equals("terminate")) {
							//System.out.println("Task " + tasks[i].taskID + ": terminate");
							tasks[i].finished = true;
							tasks[i].finishcycle = cycle;
						}
						
						else if(activityarr[0].equals("compute")) {
							//the first time a compute occurs
							int taskn = i;
							int computeTime = Integer.parseInt(activityarr[2]) - 1;
							
							tasks[i].computing = computeTime;
							tasks[i].Activities.remove(0);//remove the compute activity
							
							if(tasks[i].computing == 0 && tasks[i].Activities.get(0).contains("terminate")) {
								//set finish time
								tasks[i].finished = true;
								tasks[i].finishcycle = cycle;
							}
							
							total_current_activities += 1;
						}
						
						else {}
					}
					
					else { //the task still needs to do computing/ the task computing time > 0
						tasks[i].computing -= 1;
						
						if(tasks[i].computing == 0 && tasks[i].Activities.get(0).contains("terminate")) {
							tasks[i].finished = true;
							tasks[i].finishcycle = cycle;
						}
						
						total_current_activities += 1;
					}
				}
			}//end of going through each task
			//System.out.println();
			
			//distribute the released resources for the next cycle
			for(int i = 0; i < releasedrcs.length; i++) {
				available[i] += releasedrcs[i];
				releasedrcs[i] = 0;
			}
			
			//check for deadlock
			if(total_current_activities == total_blocked_tasks) {
				deadlock = true;
			}
			else {
				deadlock = false;
			}
			
			//If all tasks are either aborted or finished, exit the whole while loop
			int total_done = 0;
			
			for(int k = 0; k < tasks.length; k++) {
				if(tasks[k].aborted == true || tasks[k].finished == true) {
					total_done += 1;
				}
			}
			
			if(total_done == tasks.length) {
				//System.out.println("All tasks either finished or aborted");
				AllFinished = true;
			}
			
		}//end of while loop
		
		//System.out.println();
		
		
		//----------------------PRINTING SUMMARIES----------------------------
		int Twait = 0;
		int Tfinish = 0;
		System.out.print("       FIFO\n");
		for(int y = 0; y < tasks.length; y++) {
			if(tasks[y].aborted == false) {
				System.out.print("Task ");	
				//System.out.println("Task " + tasks[y].taskID + " finish time is " + tasks[y].finishcycle);
				//System.out.println("Task " + tasks[y].taskID + " wait time is " + tasks[y].waittime);
				if(tasks[y].finishcycle != 0) {
					double percent = ((double)tasks[y].waittime / (double)tasks[y].finishcycle) * 100;
					System.out.format("%1d%5d%5d%6s", tasks[y].taskID, tasks[y].finishcycle, tasks[y].waittime, Math.round(percent) + "%");
				}
				else {
					System.out.format("%1d%5d%5d%6s", tasks[y].taskID, tasks[y].finishcycle, tasks[y].waittime, 0 + "%");
					//System.out.println("Task " + tasks[y].taskID + " percentage of waiting time is 0");
				}
				Twait += tasks[y].waittime;
				Tfinish += tasks[y].finishcycle;
			}
			else {
				System.out.print("Task " + tasks[y].taskID + "    aborted");
			}
			System.out.println();
		}
		
	
		//System.out.println("Total Finish Time: " + Tfinish);
		//System.out.println("Total Wait Time: " + Twait);
		double Tpercent = ((double)Twait / (double)Tfinish) * 100;
		//System.out.println("Total Percentage Wait " + Math.round(Tpercent) + "%");
		System.out.print("total ");
		System.out.format("%5d%5d%6s", Tfinish, Twait, Math.round(Tpercent) + "%");
		
		
	}
}
