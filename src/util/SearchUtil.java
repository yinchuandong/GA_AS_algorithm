package util;

public class SearchUtil {

	private static SearchUtil context = null;
	
	private SearchUtil(){
		
	}
	
	public static SearchUtil getInstance(){
		if(context == null){
			context = new SearchUtil();
		}
		return context;
	}
}
