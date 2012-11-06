package system;

public class MultiWorkerImpl {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Counting available cores...");
		int cores = Runtime.getRuntime().availableProcessors();
		System.out.println("Available: " + cores);
		for (int i = 0; i < cores; i++){
			WorkerImpl worker = new WorkerImpl();
			String[] arg = new String[2];
			arg[0]=args[0];
			worker.main(arg);
		}
	}

}
