package api;


/**
 * A general task which will be executed 
 * 
 * @author torgel
 *
 * @param <T> a task with an execute method 
 */
public interface Task<T> {
	T execute();
}
