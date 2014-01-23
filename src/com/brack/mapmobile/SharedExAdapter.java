package com.brack.mapmobile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SharedExAdapter extends BaseExpandableListAdapter {
	
	private Context context;
	List<Map<String, Object>> planGroup;
	List<List<Map<String, String>>> planChild;
	
	public SharedExAdapter (Context context,  List<Map<String, Object>> groups, List<List<Map<String, String>>> childs)
	{
		this.context = context;
		this.planGroup = groups;
		this.planChild = childs;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return planChild.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.share_ex_child, null);
		
		TextView planText = (TextView) layout.findViewById(R.id.sharedPlanText);
		Button removeBtn = (Button) layout.findViewById(R.id.removeFromPublicBtn);
		
		@SuppressWarnings("unchecked")
		String text = ((Map<String, String>)getChild(groupPosition,childPosition)).get("sharedName");
		StringBuffer textBuf = new StringBuffer(text);
		final String planName = textBuf.delete(0, text.indexOf(" ")+1).toString();
		final String pid = text.substring(0, text.indexOf(" "));
		planText.setText(planName);
		
		removeBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String position = Integer.toString(childPosition);
				new removeShared().execute(pid, planName, position);
			}
		});
		
		return layout;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		return planChild.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return planGroup.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return planGroup.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.share_ex_group, null);
		
		ImageView itemIcon = (ImageView) layout.findViewById(R.id.menuIcon);
		TextView itemText = (TextView) layout.findViewById(R.id.menuText);
		
		int icon = (Integer) planGroup.get(groupPosition).get("itemIcon");
		String text = (String) planGroup.get(groupPosition).get("itemTitle");
		
		itemIcon.setImageResource(icon);
		itemText.setText(text);
		
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
	
	private class removeShared extends AsyncTask <String, Integer, Void>
    {
    	private String removeResult;
    	private int position;
    	
    	@Override
    	protected void onPreExecute()
    	{
    		((Map2Activity) context).loadingBarRun();
    	}
    	
		@Override
		protected Void doInBackground(String... params)
		{
			position = Integer.parseInt(params[2]);
			Log.i("sharedPosition", ""+position);
			
			String url = "http://140.128.198.44/mobileApp/remove.php";
			HttpPost post = new HttpPost(url);
			
			List<NameValuePair> postParams = new ArrayList<NameValuePair>();
			postParams.add(new BasicNameValuePair("pid", params[0]));
			postParams.add(new BasicNameValuePair("planName", params[1]));
			
			try
			{
				post.setEntity(new UrlEncodedFormEntity(postParams, HTTP.UTF_8));
				HttpResponse HR = new DefaultHttpClient().execute(post);
				
				if (HR.getStatusLine().getStatusCode() == 200)
				{
					removeResult = EntityUtils.toString(HR.getEntity());
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				Log.e("RemovePostError", e.toString());
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result)
		{
			if (removeResult.contains("Exist"))
				((Map2Activity) context).shortMessage("Plan dosen't Exist!");
			else if (removeResult.contains("Successfully"))
			{
				((Map2Activity) context).shortMessage("Done!");
				((Map2Activity) context).refreshList("remove", position);
			}
			else
				((Map2Activity) context).shortMessage(removeResult);
			((Map2Activity) context).loadingBarStop();
		}
    }
}
