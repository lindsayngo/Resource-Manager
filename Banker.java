import java.util.ArrayList;

public class Banker
{
    int totaltasks;
    int totalrecources;
    int[] available; //index = resource type - 1, value = available units of that resource
    int[][] alloc; //For holding how many resources are allocated to each task
    
    //For bankers, I used matrices/double arrays because it is easier to intiate and structure tasks and resources than hashmaps
    int[][] needs;
    
    int[][]moreNeed;
    int[][]lessAlloc;
    int[] released_rcs;
    int reqtask;
    int[][] requests ;
    //Number of task's requested resource
    //used when simulating satisfying a task request



    public void initialize(int t,int r){
        totaltasks = t;
        totalrecources = r; 
        available = new int[totalrecources]; 
        alloc = new int[totaltasks][totalrecources]; 

        needs = new int[totaltasks][totalrecources];
        //Holds need pairings (task, units need)
        //row index = task id + 1, column index = resource type + 1
        //must be updated each time a task releases, requests, or initiates resource
        
        requests = new int[totaltasks][totalrecources];
        //Number of task's requested resource
        //used when checking if the state will be safe when a request is granted
    }

    public void execute(Task[] tasks, int numrcs)
    {
        int cycle = 0;
       
       	ArrayList <Task> blockedtasks = new ArrayList <Task>();
        ArrayList<Task> readytasks = new ArrayList<Task>();//Tasks that will be ready to procecute
      
        
        //start of while loop
        boolean AllFinished = false;
        while(AllFinished == false){
        	
            cycle += 1;

            readytasks.clear();
            //For when a resource activity is release
            int[][] moreNeed = new int[totaltasks][totalrecources];//when a resource releases, it will need more resources in the future
            int[][] lessAlloc = new int[totaltasks][totalrecources];//when a resource releases, it has less allocated resources
            
            for(int i=0;i<totaltasks;i++){
                for(int j = 0;j < totalrecources; j++){
                    moreNeed[i][j] = 0;
                    lessAlloc[i][j] = 0;
                }
            }
            
            //For when a resource activity is release OR when a task is aborted
            int[] released_rcs = new int[totalrecources];	//released resource in this cycle, modified when all tasks finished reading in activities
            for(int i = 0;i < released_rcs.length; i++){
                released_rcs[i] = 0;
            }
            
            //CHECK blocked tasks first
            ArrayList <Task> stillblocked = new ArrayList <Task> ();
            while(blockedtasks.isEmpty() == false){
               
                    Task curtask = blockedtasks.remove(0);
                    
                    if(banker(tasks, curtask) == true){
                        //if the current task makes the state SAFE
                        readytasks.add(curtask);
                    }
                    else{
                        //make this task wait
                        stillblocked.add(curtask);
                        //curtask.waittime += 1;
                    }
                }
            

            blockedtasks.addAll(stillblocked);
	
            for(int i=0;i<tasks.length;i++){
               
                //If is blocked then skip this task
                if(blockedtasks.contains(tasks[i]) == false && readytasks.contains(tasks[i]) == false){
                    
                    if(tasks[i].computing==0){//if the task is not computing

                    	String activity = tasks[i].Activities.get(0);
                        String[] aarr = activity.split("\\s+");
                        int tasknum = Integer.parseInt(aarr[1])-1;
                        int resource = Integer.parseInt(aarr[2])-1;
                        
                        if(activity.contains("initiate")){
                            int claim = Integer.parseInt(aarr[3]);
                            
                            //If the claim exceeds the available resource
                            //so the claim CANNOT be initiated
                            if(claim > available[resource]){
                                tasks[tasknum].aborted = true;
                            }
                            
                            //if the claim can be initiated
                            else{
                                needs[tasknum][resource] = claim;
                                tasks[i].Activities.remove(0);
                            }
                            
                            //standard check to know if the task should terminate
                            if(tasks[i].Activities.get(0).contains("terminate")){
                            	tasks[i].finished = true;
                                tasks[i].finishcycle = cycle;
                            }
                        }
					
                        else if(activity.contains("request")){
                        	//if task request made the state UNSAFE, block the task
                            if(!banker(tasks,tasks[i])){
                                blockedtasks.add(tasks[i]);
                            }
                            else {
                            	//tasks[i].Activities.remove(0);
                            }
                            if(tasks[i].Activities.get(0).contains("terminate")){
                            	tasks[i].finished = true;
                                tasks[i].finishcycle = cycle;
                            }
                        }
						
                        else if(activity.contains("release")){
                            int rel = Integer.parseInt(aarr[3]);
                            
                            //do NOT change available here, because they will not be available until
                            //the END of the cycle
                            released_rcs[resource] += rel;
                            lessAlloc[tasknum][resource] = rel;
                            moreNeed[tasknum][resource]  = rel;
                            
                            tasks[i].Activities.remove(0);
                            
                            if(tasks[i].Activities.get(0).contains("terminate")){
                            	tasks[i].finished = true;
                                tasks[i].finishcycle = cycle;
                            }
                        }
                        else if(activity.contains("compute")){
                            int comptime = Integer.parseInt(aarr[2])-1;
                            
                            //set the task to the computing time
                            tasks[tasknum].computing = comptime;
                            //move on to the next activity
                            tasks[tasknum].Activities.remove(0);
                            
                            if(tasks[i].Activities.get(0).contains("terminate") && tasks[i].computing ==0){
                            	tasks[i].finished = true;
                                tasks[i].finishcycle = cycle;
                            }
                        }
                        
                    }
                   
                    else{//if task is still computing
                    	
                        tasks[i].computing -= 1;
                        
                        if(tasks[i].computing==0 && tasks[i].Activities.get(0).contains("terminate")){
                        	tasks[i].finished = true;
                            tasks[i].finishcycle = cycle;
                        }
                    }
                
                }

            }//end of task for loop

            //distribute all released resources so that the are available for the NEXT cycle
            //we can change available now
            for(int i = 0;i < totalrecources; i++){
                available[i] += released_rcs[i];
            }
            //after released resources, you must also update each task's NEEDS and ALLOCATED
            for(int i = 0;i < totaltasks; i++){
                for(int j = 0;j < totalrecources; j++){
                    alloc[i][j] -= lessAlloc[i][j];
                    needs[i][j] += moreNeed[i][j];
                }
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
        }
       
		
		//----------------------PRINTING SUMMARIES----------------------------
		int Twait = 0;
		int Tfinish = 0;
		System.out.print("       BANKER'S\n");
		for(int y = 0; y < tasks.length; y++) {
			if(tasks[y].aborted == false) {
				System.out.print("Task ");	
				
				if(tasks[y].finishcycle != 0) {
					double percent = ((double)tasks[y].waittime / (double)tasks[y].finishcycle) * 100;
					System.out.format("%1d%5d%5d%6s", tasks[y].taskID, tasks[y].finishcycle, tasks[y].waittime, Math.round(percent) + "%");
				}
				else {
					System.out.format("%1d%5d%5d%6s", tasks[y].taskID, tasks[y].finishcycle, tasks[y].waittime, 0 + "%");
				}
				Twait += tasks[y].waittime;
				Tfinish += tasks[y].finishcycle;
			}
			else {
				System.out.print("Task " + tasks[y].taskID + "   aborted");
			}
			System.out.println();
		}

		double Tpercent = ((double)Twait / (double)Tfinish) * 100;
		System.out.print("total ");
		System.out.format("%5d%5d%6s", Tfinish, Twait, Math.round(Tpercent) + "%");
		System.out.println();
       
    }//end of execute
    
    //METHOD BANKER: CALLED UPON EITHER WHEN
    //1. Seeing if a blocked task request can be satisfied
    //2. Seeing if a task's request activity can be satisfied
    //returns true if you can satisfy the task without causing an unsafe state
    //returns false if satisfying the task will cause an unsafe state
    public Boolean banker(Task[] tasks,Task task){
        //get current activities
        String activity = task.Activities.get(0);
        String arr[] = activity.split("\\s+");
        int tasknum = Integer.parseInt(arr[1])-1;
        int req_recourse = Integer.parseInt(arr[2])-1;
        int req_units = Integer.parseInt(arr[3]);
        
        //RESET: all requests must be 0
        for(int a = 0; a < totaltasks; a++){
            for(int b = 0; b < totalrecources; b++){
                requests[tasknum][req_recourse] = 0;
            }
        }
        
        reqtask = tasknum;
        //fill requests with the requested resource off the current task
        requests[tasknum][req_recourse] = req_units;
        
        //FIRST: we must check if the task's resource request exceeds claim
        //same check as in the initiate activity block
        for(int k = 0; k < totalrecources; k++)
        {
            //if request does not exceed max claim, SATISFY it (for now)
            if(requests[reqtask][k] <= needs[reqtask][k]){
            	 available[k] -= requests[reqtask][k];
            	 needs[reqtask][k] -= requests[reqtask][k];
                 alloc[reqtask][k] += requests[reqtask][k];
                 
            }
            //if request exceeds claim, RELEASE its resources and ABORT
            else
            {
                available[k] = available[k] + requests[reqtask][k];
                needs[reqtask][k] += requests[reqtask][k];
                alloc[reqtask][k] -= requests[reqtask][k];

                task.aborted = true;
                return true;
               
            }
        }
        //SECOND CHECK: will satisfying the task cause a future deadlock?
        if(checkSafe(tasks) == true)
        	//SAFE: satisfying the task will NOT cause a future deadlock
        {
        	//Move on to the next task's activity
            task.Activities.remove(0);
            
            //reset the requested matrix back to 0 for the next task
            for(int x = 0; x < totalrecources; x++){
                requests[reqtask][x] = 0;
            }
            return true;
        }
        else //UNSAFE: satisfying the task WILL cause a future deadlock
        {
        	//release all resources requested in this cycle
        	//undo any allocations/request that was satisfied
        	//BLOCK the task
            for(int s = 0; s < totalrecources; s++)//For ALL resources of the current task
            {
                available[s] += requests[reqtask][s];//give back the resources to available
                alloc[reqtask][s] -= requests[reqtask][s];//undo the allocation of the resources
                needs[reqtask][s] += requests[reqtask][s];//the task once again needs more of the resource
            }
            
          //reset the requested matrix back to 0 for the next task
            for(int s = 0; s < totalrecources; s++){
                requests[reqtask][s] = 0;
            }
         
            task.waittime += 1;
            return false;
        }
    }
    
    //The safety algorithm:
    public boolean checkSafe(Task[] tasks)
    {
        int[] simulation_avail = new int[totalrecources];
        //represents resources that have been USED and GIVEN BACK once the task completes
        //work[i] = N means there are N instances of resource(i) that have been returned 
        for(int i = 0; i < totalrecources; i++)
        {
            simulation_avail[i] = available[i];
            //work is a simulation of available
        }
        
        boolean[] finish = new boolean[totaltasks];
        //indicates whether each task is completed or not
        //finish[i] = status of i-th task
        //finish[i] = true --> task is completed, finish[i] = false --> task is not completed
        for(int i = 0; i < totaltasks; i++){
            if(tasks[i].finished == true||tasks[i].aborted == true){
                finish[i] = true;
            }
            else{
                finish[i] = false;
            }
        }
      
        //we must find an index(task) such that
        //1. finish[index] = false    meaning that the task is NOT yet completed
        //2. needs[index] <= work[index]  meaning that the task CAN COMPLETE its max claim
        
        //we must use a do while loop so we can test the condition at the END
        int index = 0;
        //i represents the task we are currently on
        //we start at the first task and move on to the next one (when = depending on the conditions of the while loop)
        do
        {
            boolean moveon = true;//indicates if the needs exceed the currently available resources
            //if false --> the task's needs exceed the currently available resources
            //if true  --> the task's needs do not exceed the currently available resources
           
            //go through current task i's resources to see if needs[index][j] <= work[j]
            for(int j = 0; j < totalrecources; j++){
            	if(needs[index][j] <= simulation_avail [j]) {
            		moveon = true;
            	}
            	else if(needs[index][j] > simulation_avail[j]){
                    moveon = false;
                    break;//exit from checking the rest of the task's resources
                    //this task must wait for other tasks and must remain UNFINISHED --> if it never becomes finished, 
                    //we know the state is unsafe
                    //we move on to the next task; the if statement below is NOT entered because bool = false
                }
            }
           
            //if finish[index] = false && needs[index,j] <= work[j]
            if(finish[index] == false && moveon == true){
                for(int j = 0; j < totalrecources;j++){
                    simulation_avail[j] = simulation_avail[j] + alloc[index][j]; //simulating the task's release of resources
                }
                
                finish[index] = true;//task i has now been completed
                
                index = -1; //go BACKWARDS to check unfinished tasks --> sets the current task to the first one AGAIN
            
            }
            
            index += 1;
            
        }while(index < totaltasks);//check if all tasks have been checked and we can exit the while loop
        
        //we must make sure ALL tasks can complete
        //once we know this, we can return true, because the state is safe
        for(int n = 0; n < finish.length; n++) {
        	if(finish[n] == false) {
        		return false;//state is unsafe, one of the tasks could not complete
        	}
        }
        //if false was not returned
        return true; //meaning the state is SAFE, all of the tasks could complete

    }

   }
