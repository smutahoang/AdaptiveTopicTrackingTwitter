package hoang.l3s.attt.runner;

import java.util.Date;

public class Runner {
	public static void main(String[] args) {
//		hoang.l3s.attt.data.DataExamination.main(null);
		//hoang.l3s.attt.data.NewsMediaTweets.main(null);
		
		hoang.l3s.attt.data.LipengRen ren = new hoang.l3s.attt.data.LipengRen();
		Date nowTime = new Date();
		System.out.println("current time:" + nowTime);
//		ren.test();
		ren.main(null);
		Date nowTime2 = new Date();
		System.out.println("current time:" + nowTime2);
	}
}
