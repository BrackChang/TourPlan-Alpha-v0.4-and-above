package com.brack.mapmobile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
//import android.support.v4.util.LruCache;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ExAdapter extends BaseExpandableListAdapter {

private Context context;
private List<Map<String, Object>> spotGroup;
private List<List<Map<String, String>>> spotChild;

private String userName;
private String Pid;
boolean online;
private ArrayList<String> thumbList;
protected static final int REFRESH_DATA = 0x00000001;
//private ArrayList<Object> imageList;
//public ImageLoader_bean imageLoader_bean;
public ImageLoader imageLoader;

private List<Map<String, Object>> originalList;
private ArrayList<String> originalThumbList;
private boolean filterable;

private int screenWidth;
private int screenHeight;
private double screenSize;
@SuppressWarnings("deprecation")
int SDKVersion = Integer.parseInt(VERSION.SDK);

private CountDownTimer cd;
private String spotName;
private int imageNum;
private int downloadCount;
private int downloadPos;

private int imageWidth;
private int imageHeight;
private boolean needBackup;
private boolean notFinishYet;

private TextView loadNum;
private Bitmap mapBitmap;

//private LruCache<String, Bitmap> memCache;

	public ExAdapter (Context context, List<Map<String, Object>> groups, List<List<Map<String, String>>> childs,
						boolean online, String userName, String Pid)
	{
		this.spotGroup = groups;
		this.spotChild = childs;
		this.online = online;
		this.userName = userName;
		this.Pid = Pid;
		this.context = context;
		
		this.originalList = new ArrayList<Map<String, Object>>();
		this.originalThumbList = new ArrayList<String>();
		this.originalList.addAll(spotGroup);
		
		//imageLoader_bean = new ImageLoader_bean(context.getApplicationContext());
		imageLoader = new ImageLoader(context.getApplicationContext());
		//imageList = new ArrayList<Object>();
		thumbList = new ArrayList<String>();
		if (online) {
			loadNum = (TextView) ((Map2Activity)context).findViewById(R.id.downloadCount);
			getAllThumbUrl();
		}
		
		DisplayMetrics DM = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(DM);
		double diagonalPixels = Math.sqrt((Math.pow(DM.widthPixels, 2) + Math.pow(DM.heightPixels, 2)));
        double screen = diagonalPixels / (160 * DM.density);
        
        screenSize = screen;
        screenWidth = DM.widthPixels;
        screenHeight = DM.heightPixels;
        
        Log.i("exScreenSize", ""+screenSize);
        Log.i("exScreenWidth", ""+screenWidth);
        Log.i("exScreenHeight", ""+screenHeight);
		
		if (screenSize < 4)
		{
			this.imageWidth = 200;
			this.imageHeight = 150;
		}
		else if (screenSize >=4 && screenSize < 6.5)
		{
			if (screenWidth <= 800)
			{
				this.imageWidth = 300;
				this.imageHeight = 250;
			} else
			{
				this.imageWidth = 450;
				this.imageHeight = 400;
			}
		}
		else {
			if (screenWidth <= 800)
			{
				this.imageWidth = 350;
				this.imageHeight = 300;				
			} else
			{
				this.imageWidth = 450;
				this.imageHeight = 400;
			}
		}
		
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo mi = new MemoryInfo();
		am.getMemoryInfo(mi);
		final int maxMemory = am.getMemoryClass() * 1024;
		//final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		final int totalMemory = (int) (Runtime.getRuntime().totalMemory() / 1024);
		final int freeMemory = (int) (Runtime.getRuntime().freeMemory() / 1024);
		final int cacheSize = maxMemory / 8;
		final long availableMem = mi.availMem;
		Log.i("MaxMemory",""+ maxMemory / 1024 + " MB");
		Log.i("TotalMemory",""+ totalMemory / 1024 + " MB");
		Log.i("FreeMemory",""+ freeMemory / 1024 + " MB");
		Log.i("CacheSize",""+ cacheSize / 1024 + " MB");
		Log.i("AvailableRam",""+ Formatter.formatFileSize(context, availableMem));
		/*
		memCache = new LruCache<String, Bitmap>(cacheSize) {
			@SuppressLint("NewApi")
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getByteCount() / 1024;
			}
		};
		*/
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return spotChild.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.ex_child_layout, null);
		
		@SuppressWarnings("unchecked")
		String itemText = ((Map<String, String>)getChild(groupPosition,childPosition)).get("info");
		
		TextView itemContent = (TextView) layout.findViewById(R.id.spotInfo);
		itemContent.setText(itemText);
		
		final ImageView spotImage1 = (ImageView) layout.findViewById(R.id.spotImage1);
		final ImageView spotImage2 = (ImageView) layout.findViewById(R.id.spotImage2);
		ImageButton imageDownloadBtn = (ImageButton) layout.findViewById(R.id.imageDownloadBtn);
		if (!online)
			imageDownloadBtn.setVisibility(View.GONE);
		final ImageButton imageDeleteBtn = (ImageButton) layout.findViewById(R.id.imageDeleteBtn);
		
		final String titleText = (String) spotGroup.get(groupPosition).get("title");
		
		final File SDpath = new File(Environment.getExternalStorageDirectory().getPath());
		
		try
		{
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			{
				File imageFile1 = new File(SDpath + "/tourPlanSaved/imageSaved/" + titleText + "1.jpg");
				File imageFile2 = new File(SDpath + "/tourPlanSaved/imageSaved/" + titleText + "2.jpg");
				
				if (imageFile1.exists())
				{
					LayoutParams params1 = spotImage1.getLayoutParams();
					params1.width = imageWidth;
					params1.height = imageHeight;
					spotImage1.setLayoutParams(params1);
					
					FileInputStream FIS1 = new FileInputStream(imageFile1);
					//Bitmap bitmap1 = BitmapFactory.decodeStream(FIS1);
					
					BitmapFactory.Options option = new BitmapFactory.Options();
					option.inSampleSize = 4;
					Bitmap reSizeBitmap = BitmapFactory.decodeStream(FIS1, null, option);
					Log.i("reSizeBitmap1", reSizeBitmap.getWidth() + " x " + reSizeBitmap.getHeight());
					
					spotImage1.setImageBitmap(reSizeBitmap);
					
					imageDeleteBtn.setVisibility(View.VISIBLE);
				}
				if (imageFile2.exists())
				{
					LayoutParams params2 = spotImage2.getLayoutParams();
					params2.width = imageWidth;
					params2.height = imageHeight;
					spotImage2.setLayoutParams(params2);
					
					FileInputStream FIS2 = new FileInputStream(imageFile2);
					//Bitmap bitmap2 = BitmapFactory.decodeStream(FIS2);
					
					BitmapFactory.Options option = new BitmapFactory.Options();
					option.inSampleSize = 4;
					Bitmap reSizeBitmap = BitmapFactory.decodeStream(FIS2, null, option);
					Log.i("reSizeBitmap2", reSizeBitmap.getWidth() + " x " + reSizeBitmap.getHeight());
					
					spotImage2.setImageBitmap(reSizeBitmap);
					
					imageDeleteBtn.setVisibility(View.VISIBLE);
				}
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.e("FileNotFound", e.toString());
		}
		
		spotImage1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try
				{
					if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
					{
						File imageFile1 = new File(SDpath + "/tourPlanSaved/imageSaved/" + titleText + "1.jpg");
						Log.i("FilePath", "file://" + imageFile1);
						if (imageFile1.exists())
						{
							Intent intent = new Intent();
							intent.setAction(Intent.ACTION_VIEW);
							intent.setDataAndType(Uri.parse("file://"+imageFile1), "image/*");
							context.startActivity(intent);
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					Log.e("CannotOpenImage", e.toString());
				}
			}
		});
		spotImage2.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try
				{
					if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
					{
						File imageFile2 = new File(SDpath + "/tourPlanSaved/imageSaved/" + titleText + "2.jpg");
						Log.i("FilePath", "file://" + imageFile2);
						if (imageFile2.exists())
						{
							Intent intent = new Intent();
							intent.setAction(Intent.ACTION_VIEW);
							intent.setDataAndType(Uri.parse("file://"+imageFile2), "image/*");
							context.startActivity(intent);
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					Log.e("CannotOpenImage", e.toString());
				}
			}
		});
		imageDownloadBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v)
			{	
				if (notFinishYet)
					((Map2Activity) context).longMessage("Please wait until the current job is done.");
				else
				{
					((Map2Activity) context).loadingBarRun();
					spotImage1.setVisibility(View.INVISIBLE);
					spotImage2.setVisibility(View.INVISIBLE);
					spotName = titleText;
					notFinishYet = true;
					imageNum = 0;
					new getImageSource().execute();
					/*
					cd = new CountDownTimer(500, 500){
						public void onFinish()
						{
							if (done == true)
							{
								LayoutParams params1 = spotImage1.getLayoutParams();
								params1.width = imageWidth;
								params1.height = imageHeight;
								spotImage1.setLayoutParams(params1);

								LayoutParams params2 = spotImage2.getLayoutParams();
								params2.width = imageWidth;
								params2.height = imageHeight;
								spotImage2.setLayoutParams(params2);
								
								Bitmap imageFromCache1 = getBitmapFromMemCache("image1");
								Bitmap imageFromCache2 = getBitmapFromMemCache("image2");
								spotImage1.setImageBitmap(imageFromCache1);
								spotImage2.setImageBitmap(imageFromCache2);
								
								spotImage1.setVisibility(View.VISIBLE);
								spotImage2.setVisibility(View.VISIBLE);
								imageDeleteBtn.setVisibility(View.VISIBLE);
								
								Log.i("ImageDownload", "DONE!!");
								
								final int usedMemory = (int) (Runtime.getRuntime().totalMemory() -
																Runtime.getRuntime().freeMemory()) / 1048576;
								Log.w("UsedMemory", ""+ usedMemory);
							}
							else {
								rounds ++;
								cd.start();
								Log.i("Round",""+rounds);
							}
						}
						public void onTick(long millisUntilFinished){}
					};
					cd.start();
					*/
				}
			}
		});
		
		imageDeleteBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v)
			{
				File imageFile1 = new File(SDpath + "/tourPlanSaved/imageSaved/" + titleText + "1.jpg");
				File imageFile2 = new File(SDpath + "/tourPlanSaved/imageSaved/" + titleText + "2.jpg");
				imageFile1.delete();
				imageFile2.delete();
				
				spotImage1.setVisibility(View.GONE);
				spotImage2.setVisibility(View.GONE);
				imageDeleteBtn.setVisibility(View.GONE);
			}
		});
		
		int usedMemory = (int) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
		int totalMemory = (int) (Runtime.getRuntime().totalMemory()) / 1048576;
		int freeMemory = (int) (Runtime.getRuntime().freeMemory()) / 1048576;
		int totalFreeMem = (int) (Runtime.getRuntime().maxMemory() - (usedMemory * 1048576)) / 1048576;
		Log.w("UsedMemory", ""+ usedMemory + " MB");
		Log.w("TotalMemory", ""+totalMemory + " MB");
		Log.w("FreeMemory", ""+freeMemory + " MB");
		Log.w("TotalFreeMem", ""+ totalFreeMem + " MB");
		
		return layout;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		return spotChild.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return spotGroup.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return spotGroup.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@SuppressLint("HandlerLeak")
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.ex_group_layout, null);
		
		final DisplayMetrics DM = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(DM);
		double diagonalPixels = Math.sqrt((Math.pow(DM.widthPixels, 2) + Math.pow(DM.heightPixels, 2)));
        final double size = diagonalPixels / (160 * DM.density);
		
		final String titleText = (String) spotGroup.get(groupPosition).get("title");
		final String titleDescrText = (String) spotGroup.get(groupPosition).get("titleDescribe");
		
		String pic1 = (String) spotGroup.get(groupPosition).get("pic1");
		String pic2 = (String) spotGroup.get(groupPosition).get("pic2");
		String pic3 = (String) spotGroup.get(groupPosition).get("pic3");
		String pic4 = (String) spotGroup.get(groupPosition).get("pic4");
		String pic5 = (String) spotGroup.get(groupPosition).get("pic5");
		
		final String lat = (String) spotGroup.get(groupPosition).get("lat");
		final String lng = (String) spotGroup.get(groupPosition).get("lng");
		
		ImageButton directRoute = (ImageButton) layout.findViewById(R.id.routeDirection);
		directRoute.setFocusable(false);
		directRoute.setFocusableInTouchMode(false);
		directRoute.setClickable(true);
		
		int next = spotGroup.size();
		
		if(groupPosition == next-1) {
			directRoute.setOnClickListener(new OnClickListener() {
				@SuppressWarnings("deprecation")
				public void onClick(View v) {
					LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View view = inflater.inflate(R.layout.route_selection, null);

					ImageButton route1 = (ImageButton) view.findViewById(R.id.route1);
					
					double width = DM.widthPixels / 4;
					double height = DM.heightPixels / 11.5;
					double btnWidth = DM.widthPixels / 4;
					Log.i("BtnWidth", ""+btnWidth);
					
					if (size >= 6.5)
					{
						btnWidth = DM.widthPixels / 4.9;
						width = DM.widthPixels / 4.9;
						height = DM.heightPixels / 15;
					}

					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
														((int)btnWidth, LayoutParams.WRAP_CONTENT);
					route1.setLayoutParams(params);

					final PopupWindow popUp = new PopupWindow (view, (int)width, (int)height);
					popUp.setFocusable(true);
					popUp.setOutsideTouchable(true);
					popUp.setBackgroundDrawable(new BitmapDrawable());

					double xpos = DM.widthPixels / 8 - popUp.getWidth();
					double ypos = DM.heightPixels / 50 - (popUp.getHeight() / 0.5);
					
					if (size >= 6.5)
					{
						ypos = DM.heightPixels / 50 - (popUp.getHeight() / 0.42);
					}

					popUp.showAsDropDown(v, (int)xpos, (int)ypos);

					route1.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							((Map2Activity) context).routeFromMyPosition(lat, lng);
							((Map2Activity) context).mapHalf();
							InputMethodManager imm = (InputMethodManager)v.getContext().
													getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
							popUp.dismiss();
						}
					});		
				}
			});
		}
		else {
			final String nextLat = (String) spotGroup.get(groupPosition+1).get("lat");
			final String nextLng = (String) spotGroup.get(groupPosition+1).get("lng");
			
			directRoute.setOnClickListener(new OnClickListener() {
				@SuppressWarnings("deprecation")
				public void onClick(View v) {
					LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View view = inflater.inflate(R.layout.route_selection, null);
					
					ImageButton route1 = (ImageButton) view.findViewById(R.id.route1);
					ImageButton route2 = (ImageButton) view.findViewById(R.id.route2);
					
					double width = DM.widthPixels / 1.9;
					double height = DM.heightPixels / 11.5;
					double btnWidth = DM.widthPixels / 4;
					Log.i("BtnWidth", ""+btnWidth);
					
					if (size >= 6.5)
					{
						btnWidth = DM.widthPixels / 4.9;
						width = DM.widthPixels / 2.355;
						height = DM.heightPixels / 15;
					}
					
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
														((int)btnWidth, LayoutParams.WRAP_CONTENT);
					route1.setLayoutParams(params);
					route2.setLayoutParams(params);
					
					final PopupWindow popUp = new PopupWindow (view, (int)width, (int)height);
					popUp.setFocusable(true);
					popUp.setOutsideTouchable(true);
					popUp.setBackgroundDrawable(new BitmapDrawable());
					
					double xpos = DM.widthPixels / 5 - popUp.getWidth();
					double ypos = DM.heightPixels / 50 - (popUp.getHeight() / 0.5);
					
					if (size >= 6.5)
					{
						ypos = DM.heightPixels / 50 - (popUp.getHeight() / 0.42);
					}
					
					popUp.showAsDropDown(v, (int)xpos, (int)ypos);
					
					final InputMethodManager imm = (InputMethodManager)v.getContext().
													getSystemService(Context.INPUT_METHOD_SERVICE);
					
					route1.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							((Map2Activity) context).routeFromMyPosition(lat, lng);
							((Map2Activity) context).mapHalf();
							imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
							popUp.dismiss();
						}
					});
					route2.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							((Map2Activity) context).routeToNextSpot(lat, lng, nextLat, nextLng);
							((Map2Activity) context).mapHalf();
							imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
							popUp.dismiss();
						}
					});
				}
			});
		}
		
		TextView titleName = (TextView) layout.findViewById(R.id.groupName);
		TextView titleDescrName = (TextView) layout.findViewById(R.id.groupDescr);
		ImageButton showMap = (ImageButton) layout.findViewById(R.id.showMap);
		ImageView foodFlag = (ImageView) layout.findViewById(R.id.typeImage1);
		ImageView hotelFlag = (ImageView) layout.findViewById(R.id.typeImage2);
		ImageView shopFlag = (ImageView) layout.findViewById(R.id.typeImage3);
		ImageView sceneFlag = (ImageView) layout.findViewById(R.id.typeImage4);
		ImageView transFlag = (ImageView) layout.findViewById(R.id.typeImage5);
		
		titleName.setText(titleText);
		titleDescrName.setText(titleDescrText);
		
		showMap.setFocusable(false);
		showMap.setFocusableInTouchMode(false);
		showMap.setClickable(true);
		
		if (pic1 != null) foodFlag.setImageResource(R.drawable.food_icon);
		if (pic2 != null) hotelFlag.setImageResource(R.drawable.hotel_icon);
		if (pic3 != null) shopFlag.setImageResource(R.drawable.shopping_icon);
		if (pic4 != null) sceneFlag.setImageResource(R.drawable.scene_icon);
		if (pic5 != null) transFlag.setImageResource(R.drawable.transport_icon);
		
		final String queue = titleDescrText.substring(0, titleDescrText.indexOf("-"));
		if (online)
		{
			showMap.setOnClickListener(new OnClickListener() {
				public void onClick(View arg0) {
					((Map2Activity) context).exListMapMove(lat, lng);

					((Map2Activity) context).laterSave(queue + "-" + titleText);

					InputMethodManager imm = (InputMethodManager)arg0.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(arg0.getApplicationWindowToken(), 0);
				}
			});
		}
		final ImageView tbImage = (ImageView) layout.findViewById(R.id.thumbNail);
		
		final Handler loadThumb = new Handler()
		{
			@Override
			public void handleMessage(Message url)
			{
				switch (url.what)
				{
				case REFRESH_DATA:
					String urlString = null;
					if (url.obj instanceof String)
						urlString = (String) url.obj;
					imageLoader.DisplayImage(urlString, tbImage);
					break;
				}
			};
		};
		tbImage.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new Thread()
				{
					public void run()
					{
						String thumbUrl = getThumbUrl(titleText);
						loadThumb.obtainMessage(REFRESH_DATA, thumbUrl).sendToTarget();
					}
				}.start();
			}
		});
		
		/*
		if (imageList.size() == spotGroup.size() && online)
		{
			GoogleImageBean imageBean = (GoogleImageBean) imageList.get(groupPosition);
			tbImage.setTag(imageBean.getThumbUrl());
			imageLoader_bean.DisplayImage(imageBean.getThumbUrl(), context, tbImage);
		}
		*/
		if (thumbList.size() == spotGroup.size() && online)
		{
			String[] urlList = thumbList.toArray(new String[thumbList.size()]);
			imageLoader.DisplayImage(urlList[groupPosition], tbImage);
		}
		
		if (!online)
		{
			showMap.setVisibility(View.GONE);
			directRoute.setVisibility(View.GONE);
			final File SDpath = new File(Environment.getExternalStorageDirectory().getPath());
			try
			{
				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				{
					File thumbFile = new File(SDpath + "/tourPlanSaved/thumbNailSaved/" + titleText + "0.jpg");
					File mapImagePath = new File(SDpath + "/tourPlanSaved/" + userName +"/"+ Pid +"/");
					File[] mapImages = mapImagePath.listFiles();
					
					if (thumbFile.exists())
					{
						FileInputStream FIS1 = new FileInputStream(thumbFile);
						Bitmap bitmap = BitmapFactory.decodeStream(FIS1);
						tbImage.setImageBitmap(bitmap);
					}
					if (mapImagePath.exists())
					{
						for (int i = 0; i < mapImages.length; i++)
						{
							if (mapImages[i].getName().contains(queue + "-" + titleText))
								showMap.setVisibility(View.VISIBLE);
						}
					}
					showMap.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							popMapImage(queue + "-" + titleText);
							Log.i("sendMapName", queue + "-" + titleText);
						}
					});
				}
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
				Log.e("ThumbNotFound", e.toString());
			}
		}
		return layout;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public void popMapImage(String title)
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.map_image, null);
		RelativeLayout controlLayout = (RelativeLayout) ((OffLineMode) context).findViewById(R.id.controlLayout);
		ImageView mapImageView = (ImageView) view.findViewById(R.id.mapImage);
		
		final File SDpath = new File(Environment.getExternalStorageDirectory().getPath());
		try
		{
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			{
				
				final File mapImage = new File(SDpath + "/tourPlanSaved/" + userName +"/"+ Pid +"/" + title + ".png");

				FileInputStream FIS1 = new FileInputStream(mapImage);
				//Bitmap bitmap1 = BitmapFactory.decodeStream(FIS1);

				BitmapFactory.Options option = new BitmapFactory.Options();
				option.inSampleSize = 1;
				mapBitmap = BitmapFactory.decodeStream(FIS1, null, option);
				Log.i("reSizeBitmap1", mapBitmap.getWidth() + " x " + mapBitmap.getHeight());

				mapImageView.setImageBitmap(mapBitmap);
				
				mapImageView.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						Intent intent = new Intent();
						intent.setAction(Intent.ACTION_VIEW);
						intent.setDataAndType(Uri.parse("file://"+mapImage), "image/*");
						context.startActivity(intent);
					}
				});
			}
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			Log.e("FileNotFound", e.toString());
		}
		
		PopupWindow popUp = new PopupWindow (view, screenWidth, (int)(screenHeight/2.75));
		popUp.setFocusable(true);
		popUp.setOutsideTouchable(true);
		popUp.setBackgroundDrawable(new BitmapDrawable());
		
		popUp.showAsDropDown(controlLayout, 0, 8);
		popUp.setOnDismissListener(new OnDismissListener() {
			public void onDismiss() {
				mapBitmap.recycle();
			}
		});
	}
	
	public void getAllThumbUrl()
	{
		((Map2Activity) context).loadingBarRun();
		final String url = spotGroup.get(thumbList.size()).get("title").toString();
		//Log.d("thumbSize&Name", thumbList.size() + " " + url);
		loadNum.setVisibility(View.VISIBLE);
		loadNum.setText(thumbList.size()+"/"+spotGroup.size());
		new Thread()
		{
			public void run()
			{
				String tbUrl = getThumbUrl(url);
				setThumbUrl.obtainMessage(REFRESH_DATA, tbUrl).sendToTarget();
			}
		}.start();
	}
	
	@SuppressLint("HandlerLeak")
	Handler setThumbUrl = new Handler()
	{
		@Override
		public void handleMessage(Message url)
		{
			switch (url.what)
			{
			case REFRESH_DATA:
   				String urlString = null;
   				if (url.obj instanceof String)
   					urlString = (String) url.obj;
   				
   				thumbList.add(urlString);
   				
   				int tbNum = thumbList.size();
   				int spotSize = spotGroup.size();
   				
   				Log.i("ThumbUrlAdded", tbNum + "/" + spotSize);
   				loadNum.setText(tbNum + "/" + spotSize);
   				
   				if (tbNum == spotSize)
   				{
   					int failNum = 0;
   					for (int i = 0; i < tbNum; i++)
   					{
   						if (thumbList.get(i).equals("fail"))
   							failNum ++;
   					}
   					if (failNum > 0) {
   						((Map2Activity) context).longMessage("Got " + (spotSize-failNum) + ", " + failNum + " Failed!");
   						Log.i("thumbGot", "Got " + (spotSize-failNum) + ", " + failNum + " Failed!");
   					}
   					else {
   						//((Map2Activity) context).shortMessage("Got All!");
   					}
   					//putImageList();
   					
   					originalThumbList.addAll(thumbList);
   					filterable = true;
   					
   					notifyDataSetChanged();
   					loadNum.setVisibility(View.GONE);
   					((Map2Activity) context).loadingBarStop();
   					
   					ImageButton downloadAllBtn = (ImageButton)((Map2Activity)context).findViewById(R.id.downloadAllBtn);
   					downloadAllBtn.setVisibility(View.VISIBLE);
   					downloadAllBtn.setOnClickListener(new OnClickListener() {
   						public void onClick(View v) {
   							optionDialog();
   						}
   					});
   				}
   				else
   					getAllThumbUrl();
			}
		}
	};
	
	private String getThumbUrl(String spot)
	{
		String spotNameBuf = spot;
		spotNameBuf = spotNameBuf.replace(" ", "").trim();
		spotNameBuf = spotNameBuf.replace("'", "");
		spotNameBuf = spotNameBuf.replace("出發", "");
		spotNameBuf = spotNameBuf.replace("回程", "");
		spotNameBuf = spotNameBuf.replace("返回", "");
		Log.i("spotNameInput", spotNameBuf);
		
		String Url = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q="+spotNameBuf+"&rsz=1";
		HttpGet httpGet = new HttpGet(Url);
		try
		{
			HttpResponse HR = new DefaultHttpClient().execute(httpGet);

			if (HR.getStatusLine().getStatusCode() == 200)
			{
				String strResult = EntityUtils.toString(HR.getEntity());

				JSONObject jo = new JSONObject(strResult);
				JSONArray ja = jo.getJSONObject("responseData").getJSONArray("results");

				String tbUrl = ja.getJSONObject(0).getString("tbUrl").replace("\u003d", "=");
				//Log.i("tbUrlGot", tbUrl);
				return tbUrl;
			}
			else {
				Log.e("API Url Error", Url);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.e("ThumbUrlFailed", e.getMessage().toString());
		}
		return "fail";
	}
	/*
	private void putImageList()
	{
		GoogleImageBean bean;
		try 
		{
			for (int i = 0; i < spotGroup.size(); i++)
			{
				bean = new GoogleImageBean();

				bean.setThumbUrl(thumbList.get(i));
				imageList.add(bean);
			}
			Log.i("imageListSize", ""+imageList.size());
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			Log.e("thumbError", e.toString());
		}
	}
	*/
	private void getTbBitmap(String url)
	{		
		url = url.replace("\u003d", "=");
		url = url.replace("%3F", "?");
		url = url.replace("%3D", "=");
		String[] urlCheck = url.split("25");
		if (urlCheck.length > 8)
			url = url.replace("25", "");
		Log.i("FinalUrl", url);
		
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet getRequest = new HttpGet(url);
		
		try
		{
			HttpResponse HR = client.execute(getRequest);
			// Check 200 OK for success
			int statusCode = HR.getStatusLine().getStatusCode();
			
			if (statusCode != HttpStatus.SC_OK)
			{
				Log.w("ThumbImage", "Error " + statusCode + ", While retrieving bitmap from " + url);
			}
			HttpEntity entity = HR.getEntity();
			
			if (entity != null)
			{
				InputStream is = null;
				try
				{
					// Getting contents from the stream 
					is = entity.getContent();
					 // Decoding stream data back into image Bitmap that android understands
					Bitmap bitmap = BitmapFactory.decodeStream(is);
					//bitmap.recycle();
					
					saveToSD(bitmap, "0");
					downloadCount++;
				}
				finally
				{
					if (is != null)
						is.close();
					entity.consumeContent();
					Log.w("Finally", "thumbNailFinally~");
				}
			}
		}
		catch (Exception e) {
			getRequest.abort();
			Log.e("ThumbImage", "Something went wrong while retrieving bitmap from " + url +", "+ e.toString());
		}
	}
	
	private class getImageSource extends AsyncTask <Void, Void, Void>
	{
		private String imageUrl;
		private String imageUrl2;
		private String imageTitle1;
		private String imageTitle2;
		private String imageWidth1;
		private String imageHeight1;
		private String imageWidth2;
		private String imageHeight2;
		private boolean fail;
		
		@Override
		protected Void doInBackground(Void... params)
		{
			String spotNameBuf = spotName;
			spotNameBuf = spotNameBuf.replace(" ", "").trim();
			spotNameBuf = spotNameBuf.replace("'", "");
			spotNameBuf = spotNameBuf.replace("出發", "");
			spotNameBuf = spotNameBuf.replace("回程", "");
			spotNameBuf = spotNameBuf.replace("返回", "");
			Log.i("SpotText", spotNameBuf);

			String Url = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q="+spotNameBuf+"&rsz=4";
			HttpGet httpGet = new HttpGet(Url);
			Log.i("inputUrl", Url);
			try
			{
				HttpResponse HR = new DefaultHttpClient().execute(httpGet);

				if (HR.getStatusLine().getStatusCode() == 200)
				{
					String strResult = EntityUtils.toString(HR.getEntity());

					JSONObject jo = new JSONObject(strResult);
					JSONArray ja = jo.getJSONObject("responseData").getJSONArray("results");

					String url = ja.getJSONObject(0).getString("url");
					String url2 = ja.getJSONObject(1).getString("url");
					String title1 = ja.getJSONObject(0).getString("title");
					String title2 = ja.getJSONObject(1).getString("title");
					String width1 = ja.getJSONObject(0).getString("width");
					String height1 = ja.getJSONObject(0).getString("height");
					String width2 = ja.getJSONObject(1).getString("width");
					String height2 = ja.getJSONObject(1).getString("height");

					imageUrl = url;
					imageUrl2 = url2;
					imageTitle1 = title1;
					imageTitle2 = title2;
					imageWidth1 = width1;
					imageHeight1 = height1;
					imageWidth2 = width2;
					imageHeight2 = height2;

					needBackup = false;
					getBitmap(url, width1, height1);
					if (needBackup == true){
						String url3 = ja.getJSONObject(2).getString("url");
						String width3 = ja.getJSONObject(2).getString("width");
						String height3 = ja.getJSONObject(2).getString("height");

						imageWidth1 = width3;
						imageHeight1 = height3;
						getBitmap(url3, width1, height1);
					}
					needBackup = false;
					getBitmap(url2, width2, height2);
					if (needBackup == true){
						String url4 = ja.getJSONObject(3).getString("url");
						String width4 = ja.getJSONObject(3).getString("width");
						String height4 = ja.getJSONObject(3).getString("height");

						imageWidth2 = width4;
						imageHeight2 = height4;
						getBitmap(url4, width2, height2);
					}
					/*
					memCache.remove("image1");
					memCache.remove("image2");
					addBitmapToMemCache("image1", imageA);
					addBitmapToMemCache("image2", imageB);
					*/
				}
				else {
					Log.e("API Url Error", Url);
					fail = true;
				}
			}
			catch (Exception e)
			{
				fail = true;
				e.printStackTrace();
				Log.e("imageUrlFailed", e.getMessage().toString());
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result)
		{
			if (fail) {
				cd.cancel();
				notFinishYet = false;
				((Map2Activity) context).shortMessage("Image Url Failed!!");
			}
			else {
				Log.i("imageUrl", imageUrl);
				Log.i("imageUrl2", imageUrl2);
				Log.i("imageTitle1", imageTitle1);
				Log.i("imageTitle2", imageTitle2);
				Log.i("imageSize1", imageWidth1 + " x " + imageHeight1);
				Log.i("imageSize2", imageWidth2 + " x " + imageHeight2);
				notFinishYet = false;
				notifyDataSetChanged();
			}
			((Map2Activity) context).loadingBarStop();
		}
	}
	
	private void getBitmap(String url, String bitmapWidth, String bitmapHeight)
	{
		int width = Integer.parseInt(bitmapWidth);
		int height = Integer.parseInt(bitmapHeight);
		
		if (width > 480)
			width = 480;
		if (height > 360)
			height = 360;
		
		url = url.replace("\u003d", "=");
		url = url.replace("%3F", "?");
		url = url.replace("%3D", "=");
		String[] urlCheck = url.split("25");
		if (urlCheck.length > 8)
			url = url.replace("25", "");
		Log.i("UrlLengthCheck", ""+urlCheck.length);
		Log.i("FinalUrl", url);
		
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet getRequest = new HttpGet(url);
		
		try
		{
			HttpResponse HR = client.execute(getRequest);
			// Check 200 OK for success
			int statusCode = HR.getStatusLine().getStatusCode();
			
			if (statusCode != HttpStatus.SC_OK)
			{
				Log.w("ImageDownloader", "Error " + statusCode + ", While retrieving bitmap from " + url);
				needBackup = true;
			}
			HttpEntity entity = HR.getEntity();
			
			if (entity != null)
			{
				InputStream is = null;
				try
				{
					// Getting contents from the stream 
					is = entity.getContent();
					 // Decoding stream data back into image Bitmap that android understands
					Bitmap bitmap = BitmapFactory.decodeStream(is);
					/*
					float scaleWidth = ((float) width) / bitmap.getWidth();
					float scaleHeight = ((float) height) / bitmap.getHeight();
					Matrix matrix = new Matrix();
					matrix.postScale(scaleWidth, scaleHeight);
					Bitmap resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
					*/
					//bitmap.recycle();
					
					imageNum ++;
					String num = Integer.toString(imageNum);
					saveToSD(bitmap, num);
				}
				finally
				{
					if (is != null)
						is.close();
					entity.consumeContent();
					Log.w("Finally", "bitmapFinally~");
				}
			}
		}
		catch (Exception e) {
			getRequest.abort();
			Log.e("ImageDownloader", "Something went wrong while retrieving bitmap from " + url +", "+ e.toString());
		}
	}
	
	public void saveToSD(Bitmap bmp, String num)
	{
		File SDCard = null;
		try
		{
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
					SDCard = Environment.getExternalStorageDirectory();
			else
				return;
			
			File path = new File(SDCard.getParent() + "/" + SDCard.getName() + "/tourPlanSaved/imageSaved/");
			if (num.equals("0"))
				path = new File(SDCard.getParent() + "/" + SDCard.getName() + "/tourPlanSaved/thumbNailSaved/");
			if (!path.exists())
				path.mkdirs();
			
			File fileName = new File(path, spotName + num + ".jpg");
			
			FileOutputStream FOS = new FileOutputStream(fileName);
			
			bmp.compress(Bitmap.CompressFormat.JPEG, 100, FOS);
			
			FOS.flush();
			FOS.close();
		}
		catch (FileNotFoundException e) {
			Log.e("FileNotFound", e.toString());
		}
		catch (IOException e) {
			Log.e("IOException", e.toString());
		}
	}
	
	public void optionDialog()
	{
		final String[] options = {"This Plan and Thumbnails", "Only This Plan"};
		
		ContextThemeWrapper CTW;
		
		if (screenSize >= 6.5)
			CTW = new ContextThemeWrapper(context, R.style.dialog_LargeText);
		else
			CTW = new ContextThemeWrapper(context, R.style.dialog_smallText);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(CTW);
		
		TextView title = new TextView(context);
		title.setText("Plan Saving Options");
		title.setTextColor(context.getResources().getColor(R.color.DeepSkyBlue));
		title.setGravity(Gravity.CENTER);
		title.setPadding(0, 20, 0, 20);
		
		if (screenSize >= 6.5)
		{
			title.setTextSize(34);
			builder.setCustomTitle(title);
		} else {
			title.setTextSize(24);
			builder.setCustomTitle(title);
		}
		
		builder.setItems(options, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				switch (which)
				{
				case 0:
					downloadAllClick();
					((Map2Activity) context).saveData();
					break;
				case 1:
					((Map2Activity) context).saveData();
					break;
				}
			}
		});
		builder.show();
	}
	
	public void downloadAllClick ()
	{
		downloadCount = 0;
		downloadPos = 0;
		loadNum.setVisibility(View.VISIBLE);
		loadNum.setText(downloadPos + "/" + thumbList.size());
		downloadAllThumb();
	}
	
	public void downloadAllThumb()
	{
		((Map2Activity) context).loadingBarRun();
		
		final String thumbUrl = thumbList.get(downloadPos);
		spotName = spotGroup.get(downloadPos).get("title").toString();
		new Thread()
		{
			public void run()
			{
				getTbBitmap(thumbUrl);
				getThumbDone.obtainMessage(REFRESH_DATA, null).sendToTarget();
			}
		}.start();
	}
	@SuppressLint("HandlerLeak")
	Handler getThumbDone = new Handler()
	{
		@Override
		public void handleMessage(Message url)
		{
			switch (url.what)
			{
			case REFRESH_DATA:
				
				int failNum = 0;
				for (int i = 0; i < thumbList.size(); i++)
				{
					if (thumbList.get(i).equals("fail"))
						failNum ++;
				}
				if (downloadCount == (spotGroup.size()-failNum)) {
					loadNum.setVisibility(View.GONE);
					((Map2Activity) context).longMessage("All Thumb Get!");
					((Map2Activity) context).loadingBarStop();
				}
				else {
					downloadPos++;
					loadNum.setText(downloadPos + "/" + thumbList.size());
					downloadAllThumb();
				}
				break;
			}
		};
	};
	/*
	public void addBitmapToMemCache(String key, Bitmap bitmap)
	{
		if (getBitmapFromMemCache(key) == null)
			memCache.put(key, bitmap);
	}
	
	public Bitmap getBitmapFromMemCache(String key)
	{
		return (Bitmap) memCache.get(key);
	}
	*/
	@SuppressLint("DefaultLocale")
	public void filterData(String query)
	{
		if (online)
		{
			if (filterable)
			{
				spotGroup.clear();
				thumbList.clear();

				if(query.equals(""))
				{
					spotGroup.addAll(originalList);
					thumbList.addAll(originalThumbList);
				}
				else
				{
					for (int i = 0; i < originalList.size(); i++)
					{
						if (originalList.get(i).get("title").toString().toLowerCase().contains(query.toLowerCase()) ||
								originalList.get(i).get("titleDescribe").toString().contains(query.toUpperCase()))
						{
							spotGroup.add(originalList.get(i));
							thumbList.add(originalThumbList.get(i));
						}
					}
				}
				notifyDataSetChanged();			
			}
			else {
				((Map2Activity) context).shortMessage("Wait a second! Not so fast~");
			}
		}
		else
		{
			spotGroup.clear();

			if(query.equals(""))
				spotGroup.addAll(originalList);
			else
			{
				for (int i = 0; i < originalList.size(); i++)
				{
					if (originalList.get(i).get("title").toString().toLowerCase().contains(query.toLowerCase()) ||
							originalList.get(i).get("titleDescribe").toString().contains(query.toUpperCase()))
					{
						spotGroup.add(originalList.get(i));
					}
				}
			}
			notifyDataSetChanged();		
		}
	}
}
