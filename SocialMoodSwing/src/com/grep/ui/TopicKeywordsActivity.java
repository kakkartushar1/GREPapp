package com.grep.ui;

import java.util.ArrayList;
import java.util.List;

import com.grep.database.DatabaseHandler;
import com.grep.database.Keyword;
import com.grep.database.Topic;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


/**
 * TopicKeywordsDialogFragment allows for the creation of a new topic,
 * with specified keywords, or the editing of a topic's keywords.
 * 
 * @author Gresham, Ryan, Everett, Pierce
 *
 */

public class TopicKeywordsActivity extends FragmentActivity {
	static ListView keywordsListView;
	EditText topicTitle;
	static EditText newKeywordEditText;
	static ListItemAdapter adapter;
	static List<ListItem> rows = new ArrayList<ListItem>();
	


	boolean isNewTopic;
	DatabaseHandler dh;
	boolean buttonHeightSet = false;
	static int topicId = -1;
	List<Keyword> keywords = new ArrayList<Keyword>();
	static List<Keyword> keywordTracker = new ArrayList<Keyword>();
	
	@Override
	public void onResume()
	{
		super.onResume();
		dh.open();
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		dh.close();
	}
		
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		dh = new DatabaseHandler(this);
		dh.open(); //do I have to call this here as well, was getting null pointer exceptions with database when this wasn't here
		
		//retrieve the topicId as passed to this intent from the TopicListActivity, default return is -1
		isNewTopic = getIntent().getBooleanExtra("isNewTopic", false);
		
		if (!isNewTopic) {
			topicId = getIntent().getIntExtra("topicId", -1);

			if (topicId == -1) {
				//Show user an error if the topic id is not properly retrieved... something went wrong
				//Should not ever really get here
				Toast.makeText(this, "Error: Could not find topic in database", Toast.LENGTH_LONG).show();
				this.finish();
			}
			
			keywords = dh.getAllKeywords(topicId);
		}
		
		
		
		setContentView(R.layout.keyword_dialog);
		
		//TODO give topic name as part of title
		setTitle("Topic Keywords");
		
		
		
		
				
		//Get the layout inflater
		//LayoutInflater inflater = this.getLayoutInflater();
	        
		//Get view from inflater
		//final View view = inflater.inflate(R.layout.keyword_dialog, null);
		
		//listview of keywords we will populate, edittext for the topic title, edittext for new keywords
		keywordsListView   = (ListView)findViewById(R.id.keywordsListView);
		topicTitle         = (EditText)findViewById(R.id.topicEditText);
		newKeywordEditText = (EditText)findViewById(R.id.newKeywordEditText);
				
		//since rows is static, it may need to be cleared if there were existing keyword rows left over from last view of activity
		rows.clear();
			
		//if existing topic, populate the keywords list with the keywords for this topic
	    if (!isNewTopic) {
	    	topicTitle.setText(dh.getTopic(topicId).getTopicName());
	    	
			if(keywords != null) { //shouldn't ever be null, but if this is the case, keywords.size() throws exception
				for (int i = 0; i < keywords.size(); i++)
		    	{
		      		rows.add(new ListItem(R.drawable.x, keywords.get(i).getKeyword(), keywords.get(i).getId() ));
		    	}
			}		    	
	    }
   
		//create an adapter which defines the data/format of each element of our listview
		adapter = new ListItemAdapter(this, R.layout.keywords_item_row, rows, ListItemAdapter.listItemType.KEYWORD);
	       
		//set our adapter for the listview so that we can know what each list element (row) will be like
		keywordsListView.setAdapter(adapter);
		
		
		/*
		//Build the dialog and set up the button click handlers
		AlertDialog.Builder builder = new AlertDialog.Builder(this);		
		
		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		builder.setMessage("Topic Keywords")
			.setView(view)
			//Add action buttons
			.setPositiveButton("Save Topic", null)
			.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id)
				{
					TopicKeywordsActivity.this.finish();
				}
			})
			.setNegativeButton("Delete Topic", null);
		
		final AlertDialog dialog = builder.create();		
		dialog.show();
		
		//used to set all dialog fragment buttons to the same height, it's the button with most text
		final Button deleteTopicButton = (Button) dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
		
		//set the height of the dialog fragment buttons to all be the same
		deleteTopicButton.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
	        @Override
	        public void onGlobalLayout() {
	            if (!buttonHeightSet) {
	                // Here button is already laid out and measured for the first time, so we can use height to set other buttons
	            	dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setHeight(deleteTopicButton.getHeight());
	            	dialog.getButton(AlertDialog.BUTTON_POSITIVE).setHeight(deleteTopicButton.getHeight());
	            	buttonHeightSet = true;
	                
	            }
	        }
	    });

		//onClick for Save Topic button
		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v)
			{
				String topicText = topicTitle.getText().toString();

				//if no topic name provided, highlight textedit and show warning message
				if (topicText.isEmpty()) {
					topicTitle.setHintTextColor(getResources().getColor(R.color.red));
					Toast.makeText(view.getContext(), "You need to specify a topic title!", Toast.LENGTH_SHORT).show();
				}
				else {		
					//if no keywords provided, highlight textedit and show warning message
					if (rows.isEmpty()) {
						newKeywordEditText.setHintTextColor(getResources().getColor(R.color.red));
						Toast.makeText(view.getContext(), "You must add at least one keyword!", Toast.LENGTH_SHORT).show();
					}	
					else {
						//if editing an existing topic
						if (!isNewTopic) {
							Topic topic = dh.getTopic(topicId);
							
							//if topic name has been changed in the dialog, update in db and in TopicListActivity listview
							if (!topic.getTopicName().equals(topicText)) {
								for(int i = 0; i < TopicListActivity.rows.size(); i++)
								{
									//find the changed topic by topicId in the TopicActivity listview
									if (TopicListActivity.rows.get(i).getItemId() == topicId) {
										TopicListActivity.rows.get(i).setText(topicText);
										break;
									}								
								}
								
								//update all edits to this topic
								topic.setTopicName(topicText);
								dh.updateTopic(topic);
								TopicListActivity.adapter.notifyDataSetChanged();
							}
							
							//check for newly added keywords
							for (int i = 0; i < rows.size(); i++)
							{
								int keywordId = rows.get(i).getItemId();
								if (keywordId == 0) {
									Keyword keyword = new Keyword(topicId, rows.get(i).getText());
									dh.addKeyword(keyword);
								}
							}
							
							//check for edited or deleted keywords
							if (keywords != null) {
								boolean found;
								int keywordId;
								
								//for every keyword initially loaded see if it was edited or deleted
								for(int i = 0; i < keywords.size(); i++)
								{
									found = false;
								
									for (int j = 0; j < rows.size(); j++)
									{
										keywordId = keywords.get(i).getId();

										//if the keyword is found, see if the text was edited
										if (keywords.get(i).getId() == rows.get(j).getItemId()) {
											found = true;
											
											//get the corresponding EditText for this keyword item in the listview in order to get the current text
											ListItem item = (ListItem) keywordsListView.getItemAtPosition(j);
											//EditText keywordEdit = (EditText) holder.textEdit;
											String keywordText = item.getText();
											
											//////////////what I have done
											//Change the listview from getChildAt() to getItemAtPosition() so we are pulling form rows
											//change the text to be item.getText()
											//
											
											//if text is different, update the keyword in the database
											if (!keywords.get(i).getKeyword().equals(keywordText)) {
												Keyword keyword = dh.getKeyword(keywordId);
												keyword.setKeyword(keywordText);
												dh.updateKeyword(keyword);
											}
											
											break;
										}
									}
									
									//if the keyword no longer exists in the listview, it must have been deleted so remove from db
									if (!found) {
										dh.deleteKeyword(keywords.get(i).getId());
									}
								}
							}
							
							TopicKeywordsActivity.this.finish();
						}
						else {
							//if new topic, add to db and update TopicListActivity listview
							Topic topic = new Topic(topicTitle.getText().toString());
							int topic_id = dh.addTopic(topic);
							
							for (int i = 0; i < rows.size(); i++)
							{
								Keyword keyword = new Keyword(topic_id, rows.get(i).getText());
								dh.addKeyword(keyword);
							}
														
							TopicListActivity.rows.add(new ListItem(R.drawable.edit_pencil, topic.getTopicName(), topic_id));
							TopicListActivity.adapter.notifyDataSetChanged();
							TopicListActivity.topicsListView.smoothScrollToPosition(TopicListActivity.topicsListView.getCount());
							TopicKeywordsActivity.this.finish();	
						}
					}
				}
	         }
		});
		
		//onClick for Delete Topic button
		dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v)
			{
				//if not a new topic (we are editing existing topic) we need to actually delete it; if new topic just cancel w/out saving 
				if(!isNewTopic) {
					//TODO should we pop up a warning dialog confirming that they want to delete the topic?
					//delete topic from the database
					dh.deleteTopic(topicId);
					
					//delete topic from the topics listview and update the listview
					for(int i = 0; i < TopicListActivity.rows.size(); i++)
					{
						if (TopicListActivity.rows.get(i).getItemId() == topicId) {
							TopicListActivity.rows.remove(i);
							TopicListActivity.adapter.notifyDataSetChanged();
							break;
						}
					}
				}
				
				TopicKeywordsActivity.this.finish();
			}
		});
		*/
	}
	
	
	/**
	 * When add keyword button is clicked, validate that there is a keyword to add. If no keyword, provide notification
	 * to the user. If keyword is provided, add it to the beginning of the keywords listview, and update the display.
	 */
	public void onClickAddKeywordButton(View v)
	{
		String keywordText = TopicKeywordsActivity.newKeywordEditText.getText().toString(); 
		
		if(!keywordText.isEmpty()) {
			//the last arg of the ListItem constructor is the keyword id, for new keywords set it to 0 initially
			TopicKeywordsActivity.rows.add(0, new ListItem(R.drawable.x, keywordText, 0));
			TopicKeywordsActivity.newKeywordEditText.setText("");
			TopicKeywordsActivity.newKeywordEditText.setHintTextColor(getResources().getColor(R.color.black));
			ListItemAdapter.keywordJustAdded = true;
			TopicKeywordsActivity.adapter.notifyDataSetChanged();
			//ListItemAdapter.keywordJustAdded = false;
			TopicKeywordsActivity.keywordsListView.smoothScrollToPosition(0);
		}
		else {
			TopicKeywordsActivity.newKeywordEditText.setHintTextColor(getResources().getColor(R.color.red));
			Toast.makeText(this, "Please enter a keyword to add to the list!", Toast.LENGTH_SHORT).show();
		}
	}
	
	
	/**
	 * Upon tapping delete keyword button, get the position of
	 * the button in the list, remove it, and update the listview. 
	 * The database will be updated upon clicking Save Topic
	 */
	public void onClickDeleteKeywordButton(View v)
	{
		//the delete keyword button has a tag set in the background for identifying which position in the listview it is
		int buttonRow = (Integer) v.getTag();

		TopicKeywordsActivity.rows.remove(buttonRow);
		System.out.println(((Integer)buttonRow).toString()+ "deleted" );
		//ListItemAdapter.keywordDeleted = true;
		ListItemAdapter.keywordDeleted = buttonRow;
		TopicKeywordsActivity.adapter.notifyDataSetChanged();
		//ListItemAdapter.keywordDeleted = -1;
	}	
	
	//TODO comment
	public void onClickCancelButton(View v)
	{
		TopicKeywordsActivity.this.finish();
	}
	
	
	//TODO
	public void onClickDeleteTopicButton(View v)
	{
		//if not a new topic (we are editing existing topic) we need to actually delete it; if new topic just cancel w/out saving 
		if(!isNewTopic) {
			//TODO should we pop up a warning dialog confirming that they want to delete the topic?
			//delete topic from the database
			dh.deleteTopic(topicId);
			
			//delete topic from the topics listview and update the listview
			for(int i = 0; i < TopicListActivity.rows.size(); i++)
			{
				if (TopicListActivity.rows.get(i).getItemId() == topicId) {
					TopicListActivity.rows.remove(i);
					TopicListActivity.adapter.notifyDataSetChanged();
					break;
				}
			}
		}
		
		TopicKeywordsActivity.this.finish();
	}
		
	
	//TODO comment
	public void onClickSaveTopicButton(View v)
	{
		String topicText = topicTitle.getText().toString();
 
		//if no topic name provided, highlight textedit and show warning message
		if (topicText.isEmpty()) {
			topicTitle.setHintTextColor(getResources().getColor(R.color.red));
			Toast.makeText(this, "You need to specify a topic title!", Toast.LENGTH_SHORT).show();
		}
		else {		
			//if no keywords provided, highlight textedit and show warning message
			if (rows.isEmpty()) {
				newKeywordEditText.setHintTextColor(getResources().getColor(R.color.red));
				Toast.makeText(this, "You must add at least one keyword!", Toast.LENGTH_SHORT).show();
			}	
			else {
				//if editing an existing topic
				if (!isNewTopic) {
					Topic topic = dh.getTopic(topicId);
					
					//if topic name has been changed in the dialog, update in db and in TopicListActivity listview
					if (!topic.getTopicName().equals(topicText)) {
						for(int i = 0; i < TopicListActivity.rows.size(); i++)
						{
							//find the changed topic by topicId in the TopicActivity listview
							if (TopicListActivity.rows.get(i).getItemId() == topicId) {
								TopicListActivity.rows.get(i).setText(topicText);
								break;
							}								
						}
						
						//update all edits to this topic
						topic.setTopicName(topicText);
						dh.updateTopic(topic);
						TopicListActivity.adapter.notifyDataSetChanged();
					}
					
					//check for newly added keywords
					for (int i = 0; i < rows.size(); i++)
					{
						int keywordId = rows.get(i).getItemId();
						if (keywordId == 0) {
							Keyword keyword = new Keyword(topicId, rows.get(i).getText());
							dh.addKeyword(keyword);
						}
					}
					
					//check for edited or deleted keywords
					if (keywords != null) {
						boolean found;
						int keywordId;
						
						//for every keyword initially loaded see if it was edited or deleted
						for(int i = 0; i < keywords.size(); i++)
						{
							found = false;
						
							for (int j = 0; j < rows.size(); j++)
							{
								keywordId = keywords.get(i).getId();

								//if the keyword is found, see if the text was edited
								if (keywords.get(i).getId() == rows.get(j).getItemId()) {
									found = true;
									
									//get the corresponding EditText for this keyword item in the listview in order to get the current text
									ListItem item = (ListItem) keywordsListView.getItemAtPosition(j);
									//EditText keywordEdit = (EditText) holder.textEdit;
									String keywordText = item.getText();
									
									//////////////what I have done
									//Change the listview from getChildAt() to getItemAtPosition() so we are pulling form rows
									//change the text to be item.getText()
									//
									
									//if text is different, update the keyword in the database
									if (!keywords.get(i).getKeyword().equals(keywordText)) {
										Keyword keyword = dh.getKeyword(keywordId);
										keyword.setKeyword(keywordText);
										dh.updateKeyword(keyword);
									}
									
									break;
								}
							}
							
							//if the keyword no longer exists in the listview, it must have been deleted so remove from db
							if (!found) {
								dh.deleteKeyword(keywords.get(i).getId());
							}
						}
					}
					
					TopicKeywordsActivity.this.finish();
				}
				else {
					//if new topic, add to db and update TopicListActivity listview
					Topic topic = new Topic(topicTitle.getText().toString());
					int topic_id = dh.addTopic(topic);
					
					for (int i = 0; i < rows.size(); i++)
					{
						Keyword keyword = new Keyword(topic_id, rows.get(i).getText());
						dh.addKeyword(keyword);
					}
												
					TopicListActivity.rows.add(new ListItem(R.drawable.edit_pencil, topic.getTopicName(), topic_id));
					TopicListActivity.adapter.notifyDataSetChanged();
					TopicListActivity.topicsListView.smoothScrollToPosition(TopicListActivity.topicsListView.getCount());
					TopicKeywordsActivity.this.finish();	
				}
			}
		}
     }
	
}



