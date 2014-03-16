package one.skean.booksalesinfo.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import one.skean.booksalesinfo.R;
import one.skean.booksalesinfo.baseclass.NetActivity;
import one.skean.booksalesinfo.bean.TopSale;
import one.skean.booksalesinfo.saxhandlers.TopSaleSAXHandler;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DetailActivity extends NetActivity {
	private ListView lsvDetial;
	private Button btnNextPage, btnPrevPage;
	private ViewGroup footerView;
	private TextView txvTitle, txvSubTitle;

	private String title;
	private String beginDate;
	private String endDate;
	// private String press;
	private String bookId;
	private String schoolId;

	private int type;
	private Integer currentListIndex = 0;
	private DetailAdapter detailAdapter;
	private Handler mHandler;
	private List<TopSale> tempDatas;
	private static final int MSG_NO_NEXT = 3;
	private static final int MSG_NO_PREV = 4;
	private static final int MSG_GET_DATAS = 5;
	public static final int TYPE_SCHOOL_DETAIL = 10;
	public static final int TYPE_BOOK_DETAIL = 11;
	public static final int TYPE_PRESS_DETIAL = 12;
	public static final String KEY_TITLE = "title";
	public static final String KEY_BEGIN_DATE = "beginDate";
	public static final String KEY_END_DATE = "endDate";
	public static final String KEY_PRESS = "press";
	public static final String KEY_BOOKID = "bookId";
	public static final String KEY_SCHOOLID = "schoolId";
	public static final String KEY_TYPE = "type";

	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		lsvDetial = (ListView) findViewById(R.id.lsv_detail);
		btnNextPage = (Button) findViewById(R.id.btn_next_page);
		btnPrevPage = (Button) findViewById(R.id.btn_prev_page);
		txvTitle = (TextView) findViewById(R.id.txv_title_in_detail);
		txvSubTitle = (TextView) findViewById(R.id.txv_subtitle_in_detail);
		footerView = (ViewGroup) getLayoutInflater().inflate(R.layout.lsvfooter_detail, null);
		btnNextPage.setOnClickListener(btnOnClickListener);
		btnPrevPage.setOnClickListener(btnOnClickListener);
		Bundle extras = getIntent().getExtras();
		// Bundle extras = new Bundle();
		// extras.putString(DetailActivity.KEY_TITLE, "测试用标题");
		// extras.putString(DetailActivity.KEY_BEGIN_DATE, "2012-2-22");
		// extras.putString(DetailActivity.KEY_END_DATE, "2014-2-22");
		// extras.putInt(DetailActivity.KEY_TYPE,
		// DetailActivity.TYPE_SCHOOL_DETAIL);
		// extras.putString(DetailActivity.KEY_SCHOOLID, String.valueOf(119));
		title = extras.getCharSequence(KEY_TITLE).toString();
		beginDate = extras.getString(KEY_BEGIN_DATE);
		endDate = extras.getString(KEY_END_DATE);
		type = extras.getInt(KEY_TYPE, 0);
		switch (type) {
		case TYPE_BOOK_DETAIL:
			bookId = extras.getCharSequence(KEY_BOOKID).toString();
			break;
		case TYPE_SCHOOL_DETAIL:
			schoolId = extras.getCharSequence(KEY_SCHOOLID).toString();
			break;
		// case TYPE_PRESS_DETIAL:
		// press = extras.getCharSequence(KEY_PRESS).toString();
		// break;
		}
		txvTitle.setText(title);
		txvSubTitle.setText(beginDate + " 至 " + endDate + " 销售详细:");
		mHandler = new Handler(this);
		detailAdapter = new DetailAdapter(new ArrayList<TopSale>());
		lsvDetial.addFooterView(footerView);
		lsvDetial.setAdapter(detailAdapter);
		getNextDatas();
	}

	private synchronized void getNextDatas() {
		new Thread() {
			public void run() {
				TopSaleSAXHandler handler = new TopSaleSAXHandler();
				Map<String, String> params = new HashMap<String, String>();
				params.put("beginDate", beginDate);
				params.put("endDate", endDate);
				params.put("isDesc", "1");
				params.put("orderBy", "M");
				synchronized (currentListIndex) {
					currentListIndex += 1;
					params.put("startRow", String.valueOf(currentListIndex));
					currentListIndex += 9;
					params.put("endRow", String.valueOf(currentListIndex));
					switch (type) {
					case TYPE_BOOK_DETAIL:
						params.put("action", "queryByCity");
						params.put("bookId", bookId);
						params.put("province", "-1");
						params.put("city", "-1");
						params.put("town", "-1");
						break;
					case TYPE_SCHOOL_DETAIL:
						params.put("action", "queryByBook");
						params.put("schoolId", schoolId);
						break;
					// case TYPE_PRESS_DETIAL:
					// params.put("press", press);
					// break;
					}
					List<TopSale> details = (List<TopSale>) getWebData(params, handler);
					if (details != null) {
						if (details.isEmpty()) {
							currentListIndex -= 10;
							mHandler.sendEmptyMessage(MSG_NO_NEXT);
							return;
						}
						tempDatas = details;
						mHandler.sendEmptyMessage(MSG_GET_DATAS);
					}
				}
			}

		}.start();
	}

	private void getPrevDatas() {
		new Thread() {
			public void run() {
				TopSaleSAXHandler handler = new TopSaleSAXHandler();
				Map<String, String> params = new HashMap<String, String>();
				params.put("beginDate", beginDate);
				params.put("endDate", endDate);
				params.put("isDesc", "1");
				params.put("orderBy", "M");
				synchronized (currentListIndex) {
					currentListIndex -= 19;
					params.put("startRow", String.valueOf(currentListIndex));
					if (currentListIndex < 0) {
						currentListIndex += 19;
						mHandler.sendEmptyMessage(MSG_NO_PREV);
						return;
					}
					currentListIndex += 9;
					params.put("endRow", String.valueOf(currentListIndex));
					switch (type) {
					case TYPE_BOOK_DETAIL:
						params.put("action", "queryByCity");
						params.put("bookId", bookId);
						params.put("province", "-1");
						params.put("city", "-1");
						params.put("town", "-1");
						break;
					case TYPE_SCHOOL_DETAIL:
						params.put("action", "queryByBook");
						params.put("schoolId", schoolId);
						break;
					// case TYPE_PRESS_DETIAL:
					// params.put("press", press);
					// break;
					}
					List<TopSale> details = (List<TopSale>) getWebData(params, handler);
					if (details != null) {
						tempDatas = details;
						mHandler.sendEmptyMessage(MSG_GET_DATAS);
					}
				}
			}
		}.start();
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case MSG_GET_DATAS:
			synchronized (tempDatas) {
				detailAdapter.setDatas(tempDatas);
				detailAdapter.notifyDataSetChanged();
			}
			break;
		case MSG_NO_NEXT:
			Toast.makeText(this, "没有更多数据了...", Toast.LENGTH_SHORT).show();
			break;
		case MSG_NO_PREV:
			Toast.makeText(this, "已经是第一页了...", Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}
		return false;

	}

	private OnClickListener btnOnClickListener = new OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_next_page:
				getNextDatas();
				break;
			case R.id.btn_prev_page:
				getPrevDatas();
				break;
			}
		}
	};

	private class DetailAdapter extends BaseAdapter {
		List<TopSale> datas;

		public void setDatas(List<TopSale> datas) {
			this.datas = datas;
		}

		public DetailAdapter(List<TopSale> datas) {
			this.datas = datas;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return datas.isEmpty() ? 0 : datas.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewGroup rootView = (ViewGroup) getLayoutInflater().inflate(R.layout.lsvitem_detail,
				null);
			TextView partAView = (TextView) rootView.findViewById(R.id.lsvitem_detial_titlepart);
			TextView partBView = (TextView) rootView.findViewById(R.id.lsvitem_detial_numpart);
			TextView partCView = (TextView) rootView.findViewById(R.id.lsvitem_detial_moneyspart);
			partAView.setText(datas.get(position).getName());
			partBView.setText(datas.get(position).getNum() + "本");
			partCView.setText(datas.get(position).getMoney() + "元");
			return rootView;
		}

	}
}
