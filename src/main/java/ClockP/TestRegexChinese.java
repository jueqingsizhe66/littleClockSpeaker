package ClockP;


import java.util.regex.Pattern;

public class TestRegexChinese {
    public static void main(String[] args) {
       String strE = "kjdsksklf我拒绝的积极性积分 55ds55dgs5gxdbv56dgs";
    String pattern = "[\\u4E00-\\u9FA5]+";
    String[] splitStr = strE.split("");
    for(String str:splitStr) {
	     if(Pattern.matches(pattern, str))
	     System.out.println(str);

  }
    }

}
