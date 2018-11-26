package com.binzosoft.lib.exoplayer.subtitle;

import android.text.Html;
import android.util.Log;
import android.widget.TextView;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ParserSRTUtil {

    private final String TAG = "ParserSRTUtil";

    public ArrayList<SubtitleInfo> srtList;
    public static long lastEndTime;
    private static boolean isStart = true;
    private static String sutitlePath;
    private static String sutitleCharset;
    private static ParseSTRThread parseSTRThread;
    private static int loopStep = 0;
    private static int loopStepDefination = 5;

    private static ParserSRTUtil instance;

    private ParserSRTUtil() {

    }

    public static ParserSRTUtil getInstance() {
        if (instance == null) {
            instance = new ParserSRTUtil();
        }
        return instance;
    }

    private String getCharset(String path) {
        String charset = "UTF-8";
        /*
        CharsetDetector dec = new CharsetDetector();
        FileInputStream inputStream = null;
        BufferedInputStream bufferedInputStream = null;
        try {
            inputStream = new FileInputStream(path);
            bufferedInputStream = new BufferedInputStream(inputStream);
            byte[] temp = new byte[1024];
            bufferedInputStream.read(temp);
            dec.setText(temp);
            //dec.setText(bufferedInputStream);
            CharsetMatch match = dec.detect();
            charset = match.getName();
            Log.d(TAG, "charset:"+charset);
        } catch (IOException e) {
            e.printStackTrace();
            return "UTF-8";
        }finally {
            try {
                if(inputStream != null){
                    inputStream.close();
                }
                if(bufferedInputStream != null){
                    bufferedInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        */
        return charset;
    }

    public void clearSRTList() {
        if (srtList != null) {
            srtList.clear();
        }
    }

    public boolean loadInitSRT(InputStream inputStream) {
        if (sutitleCharset == null) {
            sutitleCharset = "UTF-8";
        }
        StringBuffer sb = null;
        BufferedReader br = null;
        StringBuffer srtBody_1 = null;
        try {
            srtList = new ArrayList<SubtitleInfo>();
            br = new BufferedReader(new InputStreamReader(inputStream, sutitleCharset));
            String line = null;

            sb = new StringBuffer();
            srtBody_1 = new StringBuffer();
            while ((line = br.readLine()) != null) {
                if (!line.equals("")) {
                    sb.append(line).append("@");
                    continue;
                }

                String[] parseStrs = sb.toString().split("@");
                if (parseStrs.length < 3) {
                    sb.delete(0, sb.length());
                    continue;
                }
                SubtitleInfo srt = new SubtitleInfo();
                // 解析开始和结束时间
                String timeTotime = parseStrs[1];
                int begin_hour = Integer.parseInt(timeTotime.substring(0, 2));
                int begin_mintue = Integer.parseInt(timeTotime.substring(3, 5));
                int begin_scend = Integer.parseInt(timeTotime.substring(6, 8));
                int begin_milli = Integer.parseInt(timeTotime.substring(9, 12));
                int beginTime = (begin_hour * 3600 + begin_mintue * 60 + begin_scend) * 1000 + begin_milli;
                int end_hour = Integer.parseInt(timeTotime.substring(17, 19));
                int end_mintue = Integer.parseInt(timeTotime.substring(20, 22));
                int end_scend = Integer.parseInt(timeTotime.substring(23, 25));
                int end_milli = Integer.parseInt(timeTotime.substring(26, 29));
                int endTime = (end_hour * 3600 + end_mintue * 60 + end_scend) * 1000 + end_milli;
                String srtBody = "";

                for (int i = 2; i < parseStrs.length; i++) {
                    if (i < parseStrs.length - 1) {
                        srtBody_1.append(parseStrs[i] + "<br>");
                    } else {
                        srtBody_1.append(parseStrs[i]);
                    }
                }

                //System.out.println("jiyongfeng:"+srtBody_1.toString());
                srt.setBeginTime(beginTime);
                srt.setEndTime(endTime);
                srt.setSrtBody(srtBody_1.toString());
                srtList.add(srt);
                /*if (srtList.size() >= loopStepDefination) {
                    break;
                }*/
                srtBody_1.delete(0, srtBody_1.length());
                sb.delete(0, sb.length());
            }
            if (srtList != null && srtList.size() > 0) {
                lastEndTime = srtList.get(srtList.size() - 1).getEndTime();
            }
            Log.i(TAG, "srtList.size()=" + srtList.size());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                br.close();
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (srtList.size() > 0) {
                return true;
            }
        }
        return false;
    }

    public boolean loadInitSRT(String path) throws IOException {
        Log.d(TAG, "loadInitSRT");
        if (srtList != null) {
            srtList.clear();
        }
        sutitlePath = path;
        sutitleCharset = getCharset(sutitlePath);
        return loadInitSRT(new FileInputStream(path));
    }

    public void startParseSTRThread() {
        stopParseSTRThread();
        isStart = true;
        if (null == parseSTRThread) {
            parseSTRThread = new ParseSTRThread();
            parseSTRThread.start();
            Log.d(TAG, "startParseSTRThread");
        }
    }

    public void stopParseSTRThread() {
        isStart = false;
        Log.d(TAG, "stopParseSTRThread start");
        if (null != this.parseSTRThread) {
            this.parseSTRThread.interrupt();
            try {
                this.parseSTRThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.parseSTRThread = null;
        }
        Log.d(TAG, "stopParseSTRThread end");
    }


    public synchronized void showSRT(long position, TextView tvSrt) {
        if (srtList == null) {
            return;
        }
        long currentPosition = position;
        if (srtList.size() == 0) {
            tvSrt.setText("");
        }

        /*for(int i=0;i<srtList.size();i++){
            SubtitleInfo srtbean =srtList.get(i);
                System.out.println("jiyongfeng:List:"+srtbean.getSrtBody());
        }*/

        /*System.out.println("jiyongfeng:srtList:"+srtList.get(srtList.size()-1).getBeginTime());
        System.out.println("jiyongfeng:srtList:"+srtList.size());
        System.out.println("jiyongfeng:srtList:"+srtList.get(srtList.size()-1).getEndTime());
        System.out.println("jiyongfeng:srtList:"+srtList.get(srtList.size()-1).getSrtBody());*/
        for (int i = 0; i < srtList.size(); i++) {
            SubtitleInfo srtbean = srtList.get(i);
            if (currentPosition > srtbean.getBeginTime() && currentPosition < srtbean.getEndTime()) {
                tvSrt.setText(Html.fromHtml(srtbean.getSrtBody()));
                //System.out.println("jiyongfeng:subtile"+srtbean.getSrtBody());
                return;
            }
        }
        tvSrt.setText("");
    }


    public String searchSubtitle(String videoPath) {
        if (videoPath.startsWith("http")) {
            return "NG";
        }
        Log.d(TAG, "videoPath" + videoPath);
        //1.list all subtitles at current directory
        String videoName = videoPath.split("\\.")[0]; //video name

        File file = new File(videoPath);
        File directory = file.getParentFile();
        if (null == directory) {
            return "NG";
        }
        File[] listFiles = directory.listFiles(new FileFilter());
        Log.d(TAG, "videoName" + videoName);
        if (listFiles != null) {
            for (File f : listFiles) {
                Log.d(TAG, "f.getAbsolutePath()" + f.getAbsolutePath());
                if (f.getAbsolutePath().contains(videoName)) {
                    Log.d(TAG, "111 f.getAbsolutePath()" + f.getAbsolutePath());
                    return f.getAbsolutePath();
                }
            }
        }
        return "NG";
    }

    class FileFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            //String exts = "srt;ssa;ass;scc;stl;xml;";
            String exts = "srt;";
            String[] temp = exts.split(";");
            for (String s : temp) {
                if (name.endsWith(s))
                    return true;
            }
            return false;
        }
    }

    private class ParseSTRThread extends Thread {
        @Override
        public void run() {
            /*while (isStart) {
                loopStep = 0;
                boolean ret = loadParseSrt();
                if(!ret){
                    break;
                }
                System.out.println("jiyongfeng:ret:"+ret);
            }*/
            boolean ret = loadParseSrt();
            Log.d(TAG, "ParseSTRThread out");
        }
    }


    private synchronized boolean loadParseSrt() {
        Log.d(TAG, "loadParseSrt" + sutitlePath);
        InputStream inputStream = null;
        StringBuffer sb = null;
        BufferedReader br = null;
        StringBuffer srtBody_1 = null;
        String line = null;
        try {
            inputStream = new FileInputStream(sutitlePath);
            br = new BufferedReader(new InputStreamReader(inputStream, sutitleCharset));
            //br = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));

            sb = new StringBuffer();
            srtBody_1 = new StringBuffer();
            while ((line = br.readLine()) != null && isStart) {
                Thread.sleep(0, 500);
                if (!line.equals("")) {
                    sb.append(line).append("@");
                    continue;
                }
                String[] parseStrs = sb.toString().split("@");
                if (parseStrs.length < 3) {
                    sb.delete(0, sb.length());
                    continue;
                }
                SubtitleInfo srt = new SubtitleInfo();
                String timeTotime = parseStrs[1];
                int begin_hour = Integer.parseInt(timeTotime.substring(0, 2));
                int begin_mintue = Integer.parseInt(timeTotime.substring(3, 5));
                int begin_scend = Integer.parseInt(timeTotime.substring(6, 8));
                int begin_milli = Integer.parseInt(timeTotime.substring(9, 12));
                int beginTime = (begin_hour * 3600 + begin_mintue * 60 + begin_scend) * 1000 + begin_milli;
                int end_hour = Integer.parseInt(timeTotime.substring(17, 19));
                int end_mintue = Integer.parseInt(timeTotime.substring(20, 22));
                int end_scend = Integer.parseInt(timeTotime.substring(23, 25));
                int end_milli = Integer.parseInt(timeTotime.substring(26, 29));
                int endTime = (end_hour * 3600 + end_mintue * 60 + end_scend) * 1000 + end_milli;
                //String srtBody = "";

                for (int i = 2; i < parseStrs.length; i++) {
                    srtBody_1.append(parseStrs[i] + "<br>");
                }

                //System.out.println("jiyongfeng:"+srtBody_1.toString());
                srt.setBeginTime(beginTime);
                srt.setEndTime(endTime);
                srt.setSrtBody(srtBody_1.toString());
                srtList.add(srt);
                /*loopStep++;
                if(loopStep >=5){
                    break;
                }*/
                srtBody_1.delete(0, srtBody_1.length());
                sb.delete(0, sb.length());
            }

            if (srtList != null && srtList.size() > 0) {
                lastEndTime = srtList.get(srtList.size() - 1).getEndTime();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                br.close();
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (srtList.size() > 0) {
                return true;
            }
        }
        return false;
    }
}