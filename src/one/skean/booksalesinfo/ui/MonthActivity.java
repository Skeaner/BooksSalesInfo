package one.skean.booksalesinfo.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import one.skean.booksalesinfo.R;
import one.skean.booksalesinfo.baseclass.NetActivity;
import one.skean.booksalesinfo.bean.SaleCount;
import one.skean.booksalesinfo.bean.TopSale;
import one.skean.booksalesinfo.saxhandlers.TopSaleSAXHandler;
import one.skean.booksalesinfo.saxhandlers.SaleCountSAXHandler;
import one.skean.booksalesinfo.util.CalendarTool;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MonthActivity extends NetActivity {
	private static final int MSG_GET_MONTH_SUCCESS = 3;
	private static final int MSG_GET_DETAIL_SUCCESS = 4;
	private static final int MSG_LOAD_MORE_TEN = 5;
	private static final int MSG_LOAD_FINISH = 6;
	private static final int MSG_LOADING = 7;
	private ListView lsvMonth;
	private TextView txvMonthMoney, txvMonthNum;
	private View lsvFooterMonth;
	private Button btnLoadDetail;
	private ViewGroup frlLoadDetail;

	private Integer currentListIndex = 0;
	private List<TopSale> tempDatas;
	private String monthBegin, monthEnd;
	private Handler mHandler;
	private DetailAdapter detailAdapter;
	private SaleCount monthSummary;
	private OnClickListener listener = new OnClickListener() {
		public void onClick(View v) {
			new Thread() {
				public void run() {
					Map<String, String> params = new HashMap<String, String>();
					params.put("action", "queryTop");
					params.put("beginDate", monthBegin);
					params.put("endDate", monthEnd);
					params.put("type", "school");
					synchronized (currentListIndex) {
						currentListIndex += 1;
						params.put("startRow", String.valueOf(currentListIndex));
						currentListIndex += 9;
						params.put("endRow", String.valueOf(currentListIndex));
						TopSaleSAXHandler handler = new TopSaleSAXHandler();
						tempDatas = (List<TopSale>) getWebData(params, handler);
						if (tempDatas != null) {
							mHandler.sendEmptyMessage(MSG_LOADING);
							if (tempDatas.isEmpty()) {
								mHandler.sendEmptyMessage(MSG_LOAD_FINISH);
								return;
							}
							detailAdapter.setDatas(tempDatas);
							mHandler.sendEmptyMessageDelayed(MSG_LOAD_MORE_TEN, 1000);
						}
					}
				}
			}.start();

		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_month);
		lsvMonth = (ListView) findViewById(R.id.lsv_month);
		lsvFooterMonth = getLayoutInflater().inflate(R.layout.lsvfooter_month, null);
		txvMonthMoney = (TextView) findViewById(R.id.txv_monthmoney);
		txvMonthNum = (TextView) findViewById(R.id.txv_monthnum);
		btnLoadDetail = (Button) findViewById(R.id.btn_loaddetail);
		btnLoadDetail.setOnClickListener(listener);
		frlLoadDetail = (ViewGroup) findViewById(R.id.frl_laod_detail);
		initMonthDate();
		lsvMonth.addFooterView(lsvFooterMonth);
		detailAdapter = new DetailAdapter(new ArrayList<TopSale>());
		lsvMonth.setAdapter(detailAdapter);
		lsvMonth.setOnItemClickListener(lsvonItemClickListener);
		mHandler = new Handler(this);
		new Thread() {
			public void run() {
				SaleCountSAXHandler handler = new SaleCountSAXHandler();
				Map<String, String> params = new HashMap<String, String>();
				params.put("action", "totalNumAndMoney");
				params.put("beginDate", monthBegin);
				params.put("endDate", monthEnd);
				monthSummary = (SaleCount) getWebData(params, handler);
				if (monthSummary != null) {
					mHandler.sendEmptyMessage(MSG_GET_MONTH_SUCCESS);
				}
			}
		}.start();
	}

	private void initMonthDate() {
		Calendar calendar = Calendar.getInstance();// 获得当前的时间
		calendar.set(Calendar.DATE, 1);// 设置为当前的月的第一天
		monthBegin = CalendarTool.parseCalendarToString(calendar);// 获得表示当前月份第一天的字符串
		calendar.add(Calendar.MONTH, 1);// 月份加1
		calendar.add(Calendar.DATE, -1);// 日减1,就可以获得当前月的最后一天
		monthEnd = CalendarTool.parseCalendarToString(calendar); // 获得表示当前月份最后一天的字符串
	}

	public boolean handleMessage(Message msg) {
		super.handleMessage(msg);
		switch (msg.what) {
		case MSG_GET_MONTH_SUCCESS:
			txvMonthNum.setText(monthSummary.getTotalnum() == null ? "0" : monthSummary
				.getTotalnum());
			txvMonthMoney.setText(monthSummary.getTotalmoney() == null ? "0" : monthSummary
				.getTotalmoney());
			break;
		case MSG_GET_DETAIL_SUCCESS:
			detailAdapter.notifyDataSetChanged();
			break;
		case MSG_LOAD_MORE_TEN:
			btnLoadDetail.setText("加载后10条纪录");
			detailAdapter.notifyDataSetChanged();
			btnLoadDetail.setEnabled(true);
			break;
		case MSG_LOADING:
			btnLoadDetail.setText("加载中．．．");
			btnLoadDetail.setEnabled(false);
			break;
		case MSG_LOAD_FINISH:
			Toast.makeText(getApplicationContext(), "数据加载完毕", Toast.LENGTH_SHORT).show();
			frlLoadDetail.setVisibility(View.GONE);
			break;
		default:
			break;
		}
		return true;
	}

	private class DetailAdapter extends BaseAdapter {
		private List<TopSale> datas;

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
			return Long.parseLong(datas.isEmpty() ? "0" : datas.get(position).getId());
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewGroup root = (ViewGroup) getLayoutInflater().inflate(R.layout.lsvitem_month, null);
			TextView titlepartView = (TextView) root.findViewById(R.id.lsvitem_month_titlepart);
			TextView numpartView = (TextView) root.findViewById(R.id.lsvitem_month_numpart);
			TextView moneypartView = (TextView) root.findViewById(R.id.lsvitem_month_moneyspart);
			TopSale topSale = datas.get(position);
			titlepartView.setText(topSale.getName());
			numpartView.setText(topSale.getNum() + "本");
			moneypartView.setText(topSale.getMoney() + "元");
			return root;
		}
	}

	private OnItemClickListener lsvonItemClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
			TextView titleView = (TextView) arg1.findViewById(R.id.lsvitem_month_titlepart);
			intent.putExtra(DetailActivity.KEY_TITLE, titleView.getText().toString());
			intent.putExtra(DetailActivity.KEY_BEGIN_DATE, monthBegin);
			intent.putExtra(DetailActivity.KEY_END_DATE, monthEnd);
			intent.putExtra(DetailActivity.KEY_TYPE, DetailActivity.TYPE_SCHOOL_DETAIL);
			intent.putExtra(DetailActivity.KEY_SCHOOLID, String.valueOf(arg3));
			startActivity(intent);
		}
	};
}
