import java.util.ArrayList;

public class Task {
	
	int taskID;
	ArrayList<String> Activities = new ArrayList<String>();
	String status;
	boolean finished;
	boolean aborted;
	int computing;
	int finishcycle;
	int waittime;
	
	public Task(int id) {
		taskID = id;
		status = "unstarted";
		computing = 0;
		finished = false;
		aborted = false;
		finishcycle = 0;
		waittime = 0;
	}
	
	
}
