package client;

public class HttpHelper {
	@FunctionalInterface
	public interface ThrowingSupplier<T> {
		T get() throws Exception;
	}
	
	@FunctionalInterface
	public interface ThrowingRunner {
		run() throws Exception;
	}
	
	public static <T> T serverRequestHandler(ThrowingSupplier<T> request) {
		try {
			return request.get();
		} catch (Throwable e) {
			var err = e.getMessage();
			if (err.contains("400")) {
				System.out.println("client failure ...");
			} else if (err.contains("401")) {
				System.out.println("authorization failed!");
			} else if (err.contains("403")) {
				System.out.println("someone else already took that!");
			} else if (err.contains("500")) {
				System.out.println("server failure!"); 
			} else {
				System.out.println("something went terribly wrong!"); 
			}
			return null;
		}
	}
	
	public static serverRequestHandler(ThrowingRunner request) {
		serverRequestHandler(() -> {request.run(); return null;});
	}
	
	
}