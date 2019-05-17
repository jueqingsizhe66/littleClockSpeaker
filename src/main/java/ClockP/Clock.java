package ClockP;

import java.awt.AWTException;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Robot;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import cn.hutool.core.date.DateUtil;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.DateUtilities;
import com.jacob.com.Dispatch;
import com.jacob.com.Variant;

public class Clock {
    public static void main(String[] args) throws IOException {
        //首先看根据条件需要是否要打开设置窗体,如不需要则直接执行定时闹铃提示语
        Date date = getDate();
        System.out.println("传入的定时开始时间date为1："+date);
        if (date!=null) {
            Date date1 = getLatestScheduleTime(date);
            speak("您好，您已经设置过闹铃信息，如果需要修改，请在下方窗口中调整后按回车键结束，如不需调整，请关闭此窗口！");
            //传入定时任务，修改时方便关闭之前在执行的定时任务
            TimerTask task = createTimerTask();
            //flag表示是修改闹铃信息还是新设置，0表示新开启，1表示修改
            String flag = "1";
            openFrame(30,task,flag);
            speak("闹铃任务已成功开启！");
            System.out.println("最新定时任务时间date1:"+date);
            startSchedule(task,date1);
        }else {
            //如果没有创建过文件则等待录入姓名时间信息后在定时
            TimerTask task = new TimerTask() {
                public void run() {
                };
            };
            String flag = "0";
            openFrame(0,task,flag);
            speak("请您按格式输入相关设置信息后按回车键保存！");
        }
    }

    //是否显示窗体
    public static void openFrame(int seconds,TimerTask task,String flag) throws IOException {
        //弹出姓名输入界面
        JFrame f = new JFrame("被闹人设置");
        JLabel lb = new JLabel("请设置闹铃信息(格式：叶昭良,2019-5-17-12-30,5 )：");
        JTextField tf = new JTextField("");
        JLabel lb1 = new JLabel("请按下方提示输入语音闹铃的姓名、开始执行时间及执行频率(小时)！");
        JLabel lb2 = new JLabel();
        f.add(tf);
        f.add(lb1);
        f.add(lb);
        f.add(lb2);
        //tf.setEchoChar('*');
        lb1.setBounds(115, 10, 450, 35);
        lb.setBounds(5, 45, 320, 75);
        tf.setBounds(325, 65, 250, 30);
        f.setBounds(100,100,620,170);
        //如果传入的显示时间不为0，则窗体在指定的时间后关闭
        if(seconds==0) {
            f.setVisible(true);
        }else {
            f.setVisible(true);
            Map map = readInfo();
            tf.setText(map.get("name")+"，"+map.get("time")+"，"+map.get("frequency"));
                    /* try {
                            Robot r = new Robot();
                            r.delay(seconds*1000);
                            f.setVisible(false);
                        } catch (AWTException e) {
                            e.printStackTrace();
                        }     */
        }
        f.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                f.setVisible(false);
                //System.exit(0);
            }
        });
        tf.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e1){
                Map map;
                try {
                    map = readInfo();
                    JTextField tf = (JTextField)e1.getSource();
                    //tf.setText(map.get("name")+"，"+map.get("time"));
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                System.out.println(tf.getText());
                tf.setText(tf.getText());//读完回车之后，将文本框置为空
                String info = tf.getText();
                String[]infos = info.split(",");
                String regex = "([\\u4e00-\\u9fa5]+),[1-9][0-9]{3}-(0?[1-9]{1}|[1]{1}[0-2]{1})-([1-9]{1}|"+
                        "[1-2]{1}[1-9]{1}|[3]{1}[0-1]{1})-([1-9]{1}|[1]{1}[0-9]{1}|[2]{1}[0-3]{1})-([1-9]"+
                        "{1}|[1-5]{1}[0-9]{1}),([1-9]{1}|[1-2]{1}[0-4]{1})";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(info);
                if(matcher.matches()) {
                    String name = infos[0];
                    //判断输入的时间信息是否在当前时间之后
                    Date date1 = stringToDate(infos[1]);
                    Date date2 = (Calendar.getInstance()).getTime();
                    System.out.println("当前时间为date2"+date2+"------"+date1.after(date2));
                    if(name.equalsIgnoreCase("肖欣然")||name.equalsIgnoreCase("叶昭良")||
                       name.equalsIgnoreCase("肖欣然和叶昭良")||name.equalsIgnoreCase("叶昭良和肖欣然")) {
                        if(date1.after(date2)) {
                            try {
                                writeInfo(info);//写入文件信息到
                                if(flag.equalsIgnoreCase("1")) {
                                    //执行此次定时任务之前先关闭之前在运行的定时任务
                                    stopSchedule(task);
                                    /*speak("您好，请等待关闭之前的定时闹钟！！");
                                    //延时
                                    try {
                                    Robot r = new Robot();
                                    r.delay(seconds*1000);
                                    f.setVisible(false);
                                    } catch (AWTException e) {
                                    e.printStackTrace();
                                    }*/
                                    speak("您好，之前的定时闹钟已关闭，即将开启新的定时闹钟！");
                                    speak("闹铃相关信息已保存成功，接下来您将听到闹铃提醒示范：");
                                }else {
                                    speak("闹铃相关信息已保存成功，接下来您将听到闹铃提醒示范：");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println(info);
                        f.setVisible(false);
                        String words = "";
                        if(name.equalsIgnoreCase("肖欣然和叶昭良")||name.equalsIgnoreCase("叶昭良和肖欣然")) {
                            words = name+",太阳都这么高了，你们两个还在床上干嘛呢，是不是昨晚干了什么难以描述的事情消耗了太多精力啊！呵呵呵！好啦，不开玩笑啦， 起床啦，再不起床就有人来用打火机点被子啦！";
                        }else {
                            words = name+"，你个懒货，太阳晒屁股要起床啦，你看别人都在笑话你呢！";
                        }
                        for(int i=0;i<5;i++) {
                            speak(words);
                        }
                        startSchedule(createTimerTask(),date1);//定时语音提示
                    }else {
                        speak("您好您输入的时间信息有误，请正确输入相关信息！");
                    }
                }else {
                        speak("您好，你输入的被闹人姓名不正确，如果您想更改被闹人名称，请找叶昭良，其他人无法更改，谢谢！");
                    }


            }else {
                speak("您好您输入的信息格式有误，请正确输入相关信息！");
            }
        }
    }
                      );
}
    //文字转化为语音的方法
    public static void speak(String words) {
        //文字转化语音部分
        ActiveXComponent sap = new ActiveXComponent("Sapi.SpVoice");
        // Dispatch是做什么的?
        Dispatch sapo = sap.getObject();
        try {
            // 音量 0-100
            sap.setProperty("Volume", new Variant(100));
            // 语音朗读速度 -10 到 +10
            sap.setProperty("Rate", new Variant(-2));
            Variant defalutVoice = sap.getProperty("Voice");
            Dispatch dispdefaultVoice = defalutVoice.toDispatch();
            Variant allVoices = Dispatch.call(sapo, "GetVoices");
            Dispatch dispVoices = allVoices.toDispatch();
            Dispatch setvoice = Dispatch.call(dispVoices, "Item", new Variant(1)).toDispatch();
            ActiveXComponent voiceActivex = new ActiveXComponent(dispdefaultVoice);
            ActiveXComponent setvoiceActivex = new ActiveXComponent(setvoice);
            Variant item = Dispatch.call(setvoiceActivex, "GetDescription");
            // 执行朗读
            Dispatch.call(sapo, "Speak", new Variant(words));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sapo.safeRelease();
            sap.safeRelease();
        }

    }



    //创建文件或读文件内容并存储在map中
    public static Map readInfo() throws IOException {
        Map map = new HashMap();
        String name="";
        String time="";
        String frequency="";
        File file = new File("D:\\clock\\userInfo.txt");
        if(file.exists()) {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String info = br.readLine();
            System.out.println("info:"+info);
            if(info!=null&&info!=""&&info.length()>0) {
                String[] infos = info.split(",");
                name = infos[0];
                time = infos[1];
                frequency = infos[2];
                map.put("name", name);
                map.put("time", time);
                map.put("frequency", frequency);
            }else {
                map.put("name", "");
                map.put("time", "");
                map.put("frequency", "");
            }
        }
        return map;
    }


    //创建文件或写入文件内容
    public static void writeInfo(String info) throws IOException {
        createDir();
        createFile();
        File file = new File("D:\\clock\\userInfo.txt");
        if(file.exists()) {
            //写信息之前，先删除以前的信息
            FileWriter fw = new FileWriter(file);
            fw.write("");
            fw.flush();
            //清空之后再写入内容
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(info);
            bw.flush();
            bw.close();
        }

    }

    //创建文件夹
    public static void createDir() {
        String destDirName = "D:\\clock";
        File dir = new File(destDirName);
        if (dir.exists()) {
            System.out.println("创建目录" + destDirName + "失败，目标目录已经存在");
        }
        if (!destDirName.endsWith(File.separator)) {
            destDirName = destDirName + File.separator;
        }
        //创建目录
        if (dir.mkdirs()) {
            System.out.println("创建目录" + destDirName + "成功！");
        } else {
            System.out.println("创建目录" + destDirName + "失败！");
        }
    }


    //创建文件
    public static void createFile() {
        String fileName="D:\\clock\\userInfo.txt";
        File file = new File(fileName);
        try {
            if(!file.exists()) {
                file.createNewFile();
                System.out.print("创建文件"+fileName+"成功！");
            }else {
                System.out.print("创建文件"+fileName+"失败，该文件已存在！");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //获取存储在文件中的时间信息转化为Date
    public static Date getDate() {
        Map map = new HashMap();
        //根据闹铃时间制定定时任务时间
        try {
            map = readInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String time = (String) map.get("time");
        Date date = stringToDate(time);
        return date;
    }

    //时间字符串转化为Date
    public static Date stringToDate(String time) {
        Date date = new Date();
        date = null;
        if(time!=null&&time!="") {
            String [] strs = time.split("-");
            int year = Integer.parseInt(strs[0]);
            int month = Integer.parseInt(strs[1]);
            int day = Integer.parseInt(strs[2]);
            int hour = Integer.parseInt(strs[3]);
            int min = Integer.parseInt(strs[4]);
            System.out.println("year="+year+"month="+month+"day="+day+"hour"+hour+"min"+min);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR,year);
            calendar.set(Calendar.MONTH,month-1);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.HOUR_OF_DAY,hour);
            calendar.set(Calendar.MINUTE, min);
            calendar.set(Calendar.SECOND, 0);
            date=calendar.getTime();
            System.out.println("传入的定时开始时间calendar为2："+date);
            System.out.println("传入的定时开始时间date为2："+date);
        }
        return date;
    }


    //读取定时任务时间信息后判断是过期，保证是最新定时时间
    public static Date getLatestScheduleTime(Date date) {
        System.out.println("传入的定时开始时间date为："+date);
        //判断读取的定时任务开始时间是否低于当前时间，如果低于当前时间，需要重置定时任务的开始日期

        Date date1 = new Date();
        if(date.before(date1)) {
            //将日期的时分秒转化为秒进行比较
            //@SuppressWarnings("deprecation")
            //int millsBefor = (date.getHours())*60*60+(date.getMinutes())*60+(date.getSeconds());
            int millsBefor = (DateUtil.hour(date,true))*60*60+(DateUtil.minute(date))*60+(DateUtil.second(date));

            //int millsNow = (date1.getHours())*60*60+(date1.getMinutes())*60+(date1.getSeconds());
            int millsNow = (DateUtil.hour(date1,true))*60*60+(DateUtil.minute(date1))*60+(DateUtil.second(date1));
            System.out.println("millsBefore:----"+millsBefor+"millsNow:----"+millsNow);
            if(millsBefor<millsNow) {
                //System.out.println("date1.day:"+date1.getDay());
                System.out.println("date1.day:"+DateUtil.dayOfMonth(date1));

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY,DateUtil.hour(date,true)+24);
                calendar.set(Calendar.MINUTE, DateUtil.minute(date));
                calendar.set(Calendar.SECOND, DateUtil.second(date));
                date1=calendar.getTime();

//                date1.setHours(DateUtil.hour(date,true)+24);
//                date1.setMinutes(DateUtil.minute(date));
//                date1.setSeconds(DateUtil.second(date));
                System.out.println("新date2-----------"+date1);
            }else {

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY,DateUtil.hour(date,true));
                calendar.set(Calendar.MINUTE, DateUtil.minute(date));
                calendar.set(Calendar.SECOND, DateUtil.second(date));
                date1=calendar.getTime();
//                date1.setHours(DateUtil.hour(date,true));
//                date1.setMinutes(DateUtil.minute(date));
//                date1.setSeconds(DateUtil.second(date));
                System.out.println("新date3-----------"+date1);
            }
            System.out.println("获取的最新定时任务时间===="+date1);
            return date1;
        }else {
            return date;
        }

    }



    //创建定时任务对象
    public static TimerTask createTimerTask() {
        Map map = new HashMap();
        try {
            map = readInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String name = (String) map.get("name");
        System.out.println("name="+name);
        System.out.println(name.length());
        System.out.println("定时闹铃开始执行！");
        TimerTask task = new TimerTask() {
            public void run() {
                String words = ""; if(name.equalsIgnoreCase("肖欣然和叶昭良")||name.equalsIgnoreCase("叶昭良和肖欣然")) {
                    words = name+",太阳都这么高了，你们两个还在床上干嘛呢，是不是昨晚干了什么难以描述的事情消耗了太多精力啊！呵呵呵！好啦，不开玩笑啦，起床啦，再不 起床就有人来用打火机点被子啦！";
                }else {
                    words = name+"，你个懒货，太阳晒屁股要起床啦，你看别人都在笑话你呢！";
                }
                for(int i=0;i<5;i++) {
                    speak(words);
                }
                System.out.println("一次定时闹铃已执行完毕！");
            }
        };
        return task;
    }


    //开始定时任务方法
    public static void startSchedule(TimerTask task,Date date) {
        System.out.println("定时任务开始！");
        Map map = new HashMap();
        try {
            map = readInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String name = (String) map.get("name");
        String frequency = (String)map.get("frequency");
        int freq = Integer.parseInt(frequency);
        System.out.println("定时闹铃开始执行频率为：freq===="+freq);
        System.out.println("定时闹铃开始执行！");
        //定时任务
        Timer timer = new Timer();

        //timer.schedule(task, date, 24*60*60*1000);
        if(name!=null&&name!=""&&name.length()!=0) {
            timer.schedule(task, date, freq*60*60*1000);
        }else {
            speak("找不到闹铃的设置信息啦，快去检查一下吧！");
        }
    }



    //关闭定时任务的方法
    public static void stopSchedule(TimerTask task) {
        task.cancel();
        task = null;
        System.out.println("您好，之前的定时任务已关闭！");
    }

}
