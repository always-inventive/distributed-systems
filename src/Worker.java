import java.util.Scanner;

public class Worker {
	
	public Worker() {
		//start server
	}
	
	//pairnei ta dedomena, ta stelnei stous mappers, perimenei
	@SuppressWarnings("unused")
	public void initialize() {
		
		//anoigma serverSocket
		
		int mappers;
		int lowerLeft;
		int upperRight;
		int reducerKey; //max number of results to get from reducer
		Scanner in = new Scanner(System.in);
		
		System.out.println("Input lower left and upper right map coordinates: ");
		lowerLeft = in.nextInt();
		upperRight = in.nextInt();
		
		System.out.println("Input number of Mappers");
		mappers = in.nextInt();

		/* 
		 * apostolh stous mappers
		 */
		
		in.close();
		
	}
	
	public void waitForTasksThread() {
		
	}
	
	
}
