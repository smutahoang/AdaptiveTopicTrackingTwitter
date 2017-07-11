package hoang.l3s.attt.runner;

public class Runner {
	public static void main(String[] args) {
		// hoang.l3s.attt.data.DataExamination.main(null);
		// hoang.l3s.attt.data.NewsMediaTweets.main(null);

		hoang.l3s.attt.data.LipengRen ren = new hoang.l3s.attt.data.LipengRen();
		long startTime = System.currentTimeMillis(); 
//		ren.test();
		 ren.main(null);
		long endTime = System.currentTimeMillis(); 
		System.out.println("running time： " + (endTime - startTime) + "ms");
	}
}
