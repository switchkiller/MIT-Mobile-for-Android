package edu.mit.mitmobile2.news.view;

import java.util.ArrayList;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher.OnRefreshListener;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import edu.mit.mitmobile2.LoaderBar;
import edu.mit.mitmobile2.NewModule;
import edu.mit.mitmobile2.NewModuleActivity;
import edu.mit.mitmobile2.R;
import edu.mit.mitmobile2.news.NewsModule;
import edu.mit.mitmobile2.news.beans.NewsCategory;
import edu.mit.mitmobile2.news.beans.NewsStory;
import edu.mit.mitmobile2.news.net.CategoryProgressListener;
import edu.mit.mitmobile2.news.net.NewsDownloader;
import edu.mit.mitmobile2.news.net.StoriesProgressListener;

public class NewsTopListActivity  extends NewModuleActivity implements OnRefreshListener{

	NewsDownloader np;
	NewsArrayAdapter newsAdapter;
	ListView list;
	boolean loadCategories = true;
	private final int TOP_NR = 3;
	private PullToRefreshAttacher mRefreshAttacher;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.news_top_list);
		np = NewsDownloader.getInstance(this);
		
		createView();
	}
	/****************************************************/
	void createView() {
		list = (ListView) findViewById(R.id.newsTopStoriesLV);
		LoaderBar lb = (LoaderBar) findViewById(R.id.newsLoaderBar);
		list.setEmptyView(lb);
		newsAdapter = new NewsArrayAdapter(this,0);
		list.setAdapter(newsAdapter);
		final Context c = this;
		
		mRefreshAttacher = createPullToRefreshAttacher();
		mRefreshAttacher.setRefreshableView(list, this);
		mRefreshAttacher.setEnabled(true);
		
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
					
					final NewsStory newsCursor = (NewsStory) listView.getItemAtPosition(position);
					Intent i = null;
					if(newsCursor.getId().equals("header")){
						return;
					}else if(newsCursor.getId().equals("more")){
						i = new Intent(c, NewsCategoryListActivity.class);
					}else if(newsCursor.getCategory()!=null && newsCursor.getCategory().getId().equals("in_the_media")){
						np.showOpenInBrowserDialog(NewsTopListActivity.this,newsCursor.getSourceUrl());
						return;
					}else{
						i = new Intent(c, NewsDetailsActivity.class);
						i.putExtra(NewsDetailsActivity.STORY_ID_KEY, newsCursor.getId());
					}
					if(newsCursor.getCategory()!=null){
						i.putExtra(NewsDetailsActivity.CATEGORY_ID_KEY, newsCursor.getCategory().getId());
					}
					startActivity(i);
				}
			}
		);
		loadData(false);
	}
	private void loadData(final boolean refreshData){
		NewsDownloader.DownloadCategoriesTask dct = np.new DownloadCategoriesTask(new CategoryProgressListener(){

			@Override
			public void onPostExecute(ArrayList<NewsCategory> list) {
				if(list!=null){
					String[] cats = new String[list.size()];
					for(int i = 0;i< list.size();i++){
						NewsCategory cTmp = list.get(i);
						cats[i] = cTmp.getId();
						np.setCategory(cTmp.getId(), cTmp.getName());
					}
					NewsDownloader.DownloadStoriesTask dst = np.new DownloadStoriesTask(new StoriesProgressListener(){
						
						ArrayList<NewsStory> allStories = new ArrayList<NewsStory>();
						
						@Override
						public void onProgressUpdate(ArrayList<NewsStory>... list) {
							for(ArrayList<NewsStory> st:list){
								if(st!=null && st.size()>0){
									NewsStory nHeader = new NewsStory();
									nHeader.setId("header");
									if(st.get(0).getCategory()!=null){
										nHeader.setCategory(st.get(0).getCategory());
										nHeader.setDek(st.get(0).getCategory().getName());
									}
									allStories.add(nHeader);
									
									for(int i=0;(i < st.size() && i<TOP_NR); i++){
										allStories.add(st.get(i));
									}
									NewsStory nMore = new NewsStory();
									nMore.setId("more");
									if(st.get(0).getCategory()!=null){
										nMore.setCategory(st.get(0).getCategory());
										nMore.setDek("More "+st.get(0).getCategory().getName()+"...");
									}
									allStories.add(nMore);
	
								}
							}
							
						}

						@Override
						public void onPostExecute(Long nr) {
							newsAdapter.clear();
							for(int i=0;(i < allStories.size()); i++){
								newsAdapter.add(allStories.get(i));
							}
							mRefreshAttacher.setRefreshComplete();
						}
						
					}, "category");
					dst.setRefresh(refreshData);
					dst.execute(cats);
				}
				
			}
			
		});
		dct.setRefresh(refreshData);
		dct.execute();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	@Override
	protected NewModule getNewModule() {
		// TODO Auto-generated method stub
		return new NewsModule();
	}
	@Override
	protected boolean isScrollable() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	protected void onOptionSelected(String optionId) {
		// TODO Auto-generated method stub
		
	}
	@Override
	protected boolean isModuleHomeActivity() {
		// TODO Auto-generated method stub
		return true;
	}
	@Override
	public void onRefreshStarted(View view) {
		loadData(true);
	}	


}
