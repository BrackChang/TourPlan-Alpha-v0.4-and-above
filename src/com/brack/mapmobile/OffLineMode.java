package com.brack.mapmobile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class OffLineMode extends Activity
{
	private double screenSize;
	private int screenWidth;
	private int screenHeight;
	private PopupWindow popUp;
	private View view;
	private String[] userList;
	private ArrayList<HashMap<String, String>> userInfoList;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.offline);
		
		DisplayMetrics DM = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(DM);
		double diagonalPixels = Math.sqrt((Math.pow(DM.widthPixels, 2) + Math.pow(DM.heightPixels, 2)));
        double screen = diagonalPixels / (160 * DM.density);
        screenSize = screen;
		screenWidth = DM.widthPixels;
        screenHeight = DM.heightPixels;
        Log.i("screenSize", ""+screenSize);
        Log.i("screenWidth", ""+screenWidth);
        Log.i("screenHeight", ""+screenHeight);
        
		File dirs = getDir("offLine",Context.MODE_PRIVATE);
		File[] folders = dirs.listFiles();
		List<String> folderList = new ArrayList<String>();
		for (int i = 0; i < folders.length; i++)
		{
			folderList.add(folders[i].getName());
		}
		Log.i("Folders", ""+folderList);
		userList = folderList.toArray(new String[folderList.size()]);
		
		
		if (userList.length == 0)
		{
			AlertDialog.Builder warning = new AlertDialog.Builder(OffLineMode.this);
			
			TextView title = new TextView(this);
			title.setText("Oops!");
			title.setTextColor(getResources().getColor(R.color.DeepSkyBlue));
			title.setGravity(Gravity.CENTER);
			title.setPadding(0, 20, 0, 20);
			
			if (screenSize >= 6.5)
			{
				title.setTextSize(30);
				warning.setCustomTitle(title);
			} else {
				title.setTextSize(22);
				warning.setCustomTitle(title);
			}
			warning.setMessage("You haven't saved any user's plan yet!")
			.setCancelable(false).setPositiveButton("GO BACK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			AlertDialog dialog = warning.create();
			dialog.show();
			
			dialog.getWindow().getAttributes();
			
			TextView msgText = (TextView) dialog.findViewById(android.R.id.message);
			msgText.setGravity(Gravity.CENTER);
			Button positive = (Button) dialog.getButton(DialogInterface.BUTTON_POSITIVE);
			positive.setTextColor(getResources().getColor(R.drawable.DarkOrange));
			
			if (screenSize >= 6.5)
			{
				msgText.setTextSize(28);
				msgText.setPadding(10, 15, 10, 15);
				positive.setTextSize(28);
				
			} else {
				msgText.setTextSize(18);
				msgText.setPadding(10, 15, 10, 15);
				positive.setTextSize(18);
			}
		} else if (userList.length == 1) {
			userSpinner(0);
			getPlans(userList[0]);
		}
		else 
		{
			ArrayList<HashMap<String, String>> Lists = new ArrayList<HashMap<String, String>>();
			for (int i = 0; i < userList.length; i++)
			{
				HashMap<String, String> items = new HashMap<String, String>();
				
				items.put("userName", userList[i]);
				
				File dirInside = new File(dirs, userList[i]);
				File[] numbers = dirInside.listFiles();
				int number = (numbers.length)-1;
				String amount = Integer.toString(number);
				Log.i("planAmount", amount);
				
				if (number <= 0)
					items.put("amount", "This guy got no plan!");
				else if (number == 1)
					items.put("amount", "Got only " + amount + " plan");
				else
					items.put("amount", "Got " + amount + " plans");
				
				Lists.add(items);
			}
			userInfoList = Lists;
			
			longMessage("Now, select your user name first!");
			startCountDown();
		}
	}
	
	public void popUserList()
	{
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.expanded_list, null);
		ListView user_list = (ListView) view.findViewById(R.id.exList1); 
		RelativeLayout header = (RelativeLayout) findViewById(R.id.OffLineHeader);

		SimpleAdapter userAdapter = new SimpleAdapter
				(OffLineMode.this, userInfoList, R.layout.my_list_layout01,
						new String[] {"userName", "amount"}, new int[] {R.id.textView_1_1, R.id.textView_1_2});
		
		user_list.setAdapter(userAdapter);
		
		double width = screenWidth / 1.35;
		double height = screenHeight / 1.5;

		popUp = new PopupWindow (view, (int)width, (int)height);
		
		//popUp.setAnimationStyle(R.style.PopupAnimation);
		popUp.setFocusable(true);
		popUp.setOutsideTouchable(true);
		
		int xpos = screenWidth / 2 - popUp.getWidth() / 2;
		double ypos = (screenHeight / 2) - (popUp.getHeight() / 1.55);
		
		Log.i("Coder", "xPos:" + xpos);
		Log.i("Coder", "yPos:" + ypos);
		
		popUp.showAsDropDown(header, xpos, (int)ypos);
		
		user_list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (userInfoList.get(arg2).get("amount").toString().contains("no plan"))
					longMessage("Nothing inside!\nSave a plan at least.");
				else {
					userSpinner(arg2);
					getPlans(userList[arg2]);
					popUp.dismiss();
				}
			}
		});
	}
	
	public void userSpinner(int i)
	{
		final TextView userName = (TextView) findViewById(R.id.UserName2);
		TextView userNameText = (TextView) findViewById(R.id.userNameText);
		Spinner userNameSelector = (Spinner) findViewById(R.id.userNameSelector);
		
		userNameText.setVisibility(View.VISIBLE);
		userNameSelector.setVisibility(View.VISIBLE);
		
		ArrayAdapter<String> userAdapter = new ArrayAdapter<String>
									(OffLineMode.this, R.layout.my_spinner, userList);
		userAdapter.setDropDownViewResource(R.layout.my_spinner_dropdown);
		
		userNameSelector.setAdapter(userAdapter);
		userNameSelector.setSelection(i, true);
		userName.setText(userList[i]);
		
		userNameSelector.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				userName.setText(userList[arg2]);
				getPlans(userList[arg2]);
			}
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
	}
	
	@SuppressWarnings("deprecation")
	public void getPlans(String user)
	{
		Spinner planSelector = (Spinner) findViewById(R.id.planSelector);
		planSelector.setVisibility(View.VISIBLE);
		
		StringBuilder fileContent_plans = new StringBuilder();
		File dirs = getDir("offLine", Context.MODE_PRIVATE);
		File dir = new File(dirs, user);
		File planList = new File(dir, "planList.txt");
		
		try
		{
			FileInputStream FIS_plans = new FileInputStream(planList);
			BufferedReader planReader = new BufferedReader(new InputStreamReader(FIS_plans,"UTF-8"));
			
			String planLine = null;
			
			while ((planLine = planReader.readLine()) != null )
			{
				fileContent_plans.append(planLine).append("\n");
			}
			FIS_plans.close();
		}
		catch (OutOfMemoryError e)
		{
			e.printStackTrace();
			Log.e("FileNotFound", e.getMessage().toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.e("IOException", e.getMessage().toString());
		}
		String planXML = fileContent_plans.toString();

		File[] planIDs = dir.listFiles();
		List<String> IDList = new ArrayList<String>();
		
		for (int i = 0; i < planIDs.length; i++)
		{
			if (!planIDs[i].getName().contains("planList"))
			{
				String Pid = planIDs[i].getName().toString();
				Pid = Pid.replace("plan", "");
				Pid = Pid.replace(".txt", "");
				IDList.add(Pid);
			}
		}
		String[] idArr = IDList.toArray(new String[IDList.size()]);
		
		PlanVO planVO = XmlParser.parse(planXML);
		
		String res = "";
	        StringTokenizer stPlan = new StringTokenizer(planVO.getPlan(),",");
	        StringTokenizer stPid = new StringTokenizer(planVO.getPid(),",");
			stPlan.nextToken();
			stPid.nextToken();
				while (stPlan.hasMoreTokens() & stPid.hasMoreTokens()) {
						res = res + stPid.nextToken()+ " " + stPlan.nextToken() + " ";
				}
			StringBuffer planBuf = new StringBuffer(res);
			planBuf.deleteCharAt(0);
			String res2 = new String(planBuf);
			
			final String[] planArr2 = res2.split(":");
			
			List<String> plans = new ArrayList<String>();
			for (int i = 0; i < idArr.length; i++)
			{
				for (int j = 0; j < planArr2.length; j++)
				{
					if (planArr2[j].contains(idArr[i]))
						plans.add(planArr2[j]);
				}
			}
			String[] planArr = plans.toArray(new String[plans.size()]);
			Log.i("You got", ""+plans);
			
			String planDays = planVO.getPlanDays();
			StringBuffer planDaysBuf = new StringBuffer(planDays);
			planDaysBuf.delete(0, 5);
			String planDays2 = new String(planDaysBuf);
			String[] daysList = planDays2.split(",");
			
			String planStart = planVO.getPlanStart();
			StringBuffer planStartBuf = new StringBuffer(planStart);
			planStartBuf.delete(0, 5);
			String planStart2 = new String(planStartBuf);
			String[] startList = planStart2.split(",");
			
			String planEnd = planVO.getPlanEnd();
			StringBuffer planEndBuf = new StringBuffer(planEnd);
			planEndBuf.delete(0, 5);
			String planEnd2 = new String(planEndBuf);
			String[] endList = planEnd2.split(",");
			
			for (int i = 0; i < endList.length; i++)
			{
				endList[i] = endList[i].replace(Integer.toString(new Date().getYear()+1900)+"-", "");
			}
	}
	
	
	public void popUpClick(View v)
	{
		shortMessage(" It's Not ready yet!");
	}
	
 	public void startCountDown()
   	{
   		new CountDownTimer(1000,1000){
   			@Override
   			public void onFinish() {
   				// TODO Auto-generated method stub
   				popUserList();
   			}
   			@Override
   			public void onTick(long millisUntilFinished) {
   				// TODO Auto-generated method stub
   			}
   		}.start();
   	}
	
	public void goBackClick(View v)
	{
		finish();
	}
	
	public void userNameClick(View v)
	{
		TextView userName = (TextView) findViewById(R.id.UserName2);
		longMessage("Welcome " + userName.getText().toString() + " (£½_>£¿)");
	}
	
	private void shortMessage (String message)
    {
		View toastRoot = getLayoutInflater().inflate(R.layout.toast, null);
    	TextView toastText = (TextView) toastRoot.findViewById(R.id.myToast);
    	toastText.setText(message);
    	
    	Toast toastStart = new Toast(OffLineMode.this);
    	toastStart.setGravity(Gravity.BOTTOM, 0, 60);
    	toastStart.setDuration(Toast.LENGTH_SHORT);
    	toastStart.setView(toastRoot);
    	toastStart.show();
    }
    private void longMessage (String message)
    {
    	View toastRoot = getLayoutInflater().inflate(R.layout.toast, null);
    	TextView toastText = (TextView) toastRoot.findViewById(R.id.myToast);
    	toastText.setText(message);
    	
    	Toast toastStart = new Toast(OffLineMode.this);
    	toastStart.setGravity(Gravity.BOTTOM, 0, 60);
    	toastStart.setDuration(Toast.LENGTH_LONG);
    	toastStart.setView(toastRoot);
    	toastStart.show();
    } 
}
