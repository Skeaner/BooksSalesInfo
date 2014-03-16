package one.skean.booksalesinfo.ui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import one.skean.booksalesinfo.R;
import one.skean.booksalesinfo.baseclass.NetActivity;
import one.skean.booksalesinfo.bean.City;
import one.skean.booksalesinfo.bean.TopSale;
import one.skean.booksalesinfo.saxhandlers.CitySAXHandler;
import one.skean.booksalesinfo.saxhandlers.TopSaleSAXHandler;
import one.skean.booksalesinfo.util.CalendarTool;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class QueryActivity extends NetActivity {
	private EditText edtStartDay, edtEndDay, edtBookname, edtPress;
	private Spinner spnStatictisKinds, spnProvince, spnCity, spnCounty;
	private ViewGroup partArea, partBookname, partPress;
	private Button btnSearch;
	private ListView lsvQueryResult;
	private RadioGroup rgpOrderBy;
	private DatePicker startDatePicker, endDatePicker;
	private AlertDialog startDatePickerDialog, endDatePickerDialog;
	private ProgressDialog progressDialog;

	private CitysAdapter provinceAdapter, cityAdapter, countyAdapter;
	private QueryResultAdapter queryResultAdapter;
	private int queryFlag;
	private String queryOrderByTag = "M";
	private Calendar queryStartDay, queryEndDay;
	private DatePickerListener datePickerListener;
	private AlertDialog.Builder startDatePickerBuilder;
	private AlertDialog.Builder endDatePickerBuilder;
	private Integer currentResultListIndex;
	private List<TopSale> tempDatas;
	private boolean resetListFlag = true;
	private int firstVisible, visibleCount, totalCount;
	private Handler mHandler;

	private static final int MSG_GET_PROVINCES = 3;
	private static final int MSG_GET_CITIES = 4;
	private static final int MSG_GET_COUNTIES = 5;
	private static final int MSG_SET_DATAS = 6;
	private static final int MSG_LOAD_FINISH = 7;
	private static final int MSG_ADD_DATAS = 8;
	private static final int FLAG_QUERY_WITH_DATE = 11;
	private static final int FLAG_QUERY_WITH_SCHOOL = 12;
	private static final int FLAG_QUERY_WITH_BOOK = 13;
	private static final int FLAG_QUERY_WITH_PRESS = 14;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_query);
		edtStartDay = (EditText) findViewById(R.id.edt_startday);
		edtEndDay = (EditText) findViewById(R.id.edt_endday);
		edtBookname = (EditText) findViewById(R.id.edt_bookname);
		edtPress = (EditText) findViewById(R.id.edt_press);
		spnStatictisKinds = (Spinner) findViewById(R.id.spn_statictis_kinds);
		spnProvince = (Spinner) findViewById(R.id.spn_province);
		spnCity = (Spinner) findViewById(R.id.spn_city);
		spnCounty = (Spinner) findViewById(R.id.spn_county);
		partArea = (ViewGroup) findViewById(R.id.part_area);
		partBookname = (ViewGroup) findViewById(R.id.part_bookname);
		partPress = (ViewGroup) findViewById(R.id.part_press);
		btnSearch = (Button) findViewById(R.id.btn_search);
		rgpOrderBy = (RadioGroup) findViewById(R.id.rgp_orderby);
		lsvQueryResult = (ListView) findViewById(R.id.lsv_query_result);
		datePickerListener = new DatePickerListener();
		initDialog();
		edtStartDay.setOnClickListener(edtDaylistener);
		edtEndDay.setOnClickListener(edtDaylistener);
		spnStatictisKinds.setOnItemSelectedListener(spnStatictisKindsListener);
		btnSearch.setOnClickListener(btnSearchListener);
		spnProvince.setOnItemSelectedListener(spnProvinceListner);
		spnCity.setOnItemSelectedListener(spnCityListener);
		rgpOrderBy.setOnCheckedChangeListener(orderbyCheckedChangeListener);
		lsvQueryResult.setOnItemClickListener(lsvItemClickListener);
		mHandler = new Handler(this);
		provinceAdapter = new CitysAdapter(new ArrayList<City>(), "全部省份");
		cityAdapter = new CitysAdapter(new ArrayList<City>(), "全部市区");
		countyAdapter = new CitysAdapter(new ArrayList<City>(), "全部区域");
		queryResultAdapter = new QueryResultAdapter(new ArrayList<TopSale>());
		spnProvince.setAdapter(provinceAdapter);
		spnCity.setAdapter(cityAdapter);
		spnCounty.setAdapter(countyAdapter);
		lsvQueryResult.setAdapter(queryResultAdapter);
	}

	@Override
	public boolean handleMessage(Message msg) {
		// TODO Auto-generated method stub
		super.handleMessage(msg);
		switch (msg.what) {
		case MSG_GET_PROVINCES:
			provinceAdapter.notifyDataSetChanged();
			break;
		case MSG_GET_CITIES:
			cityAdapter.notifyDataSetChanged();
			break;
		case MSG_GET_COUNTIES:
			countyAdapter.notifyDataSetChanged();
			break;
		case MSG_SET_DATAS:
			synchronized (tempDatas) {
				queryResultAdapter.setDatas(tempDatas);
				queryResultAdapter.notifyDataSetChanged();
			}
			break;
		case MSG_LOAD_FINISH:
			Toast.makeText(this, "没有更多数据...", Toast.LENGTH_SHORT).show();
			lsvQueryResult.setOnScrollListener(null);
			break;
		case MSG_ADD_DATAS:
			synchronized (tempDatas) {
				queryResultAdapter.addDatas(tempDatas);
				queryResultAdapter.notifyDataSetChanged();
			}
			break;
		default:
			break;
		}
		if (progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		return true;
	}

	/* 初始化需要的Dialog */
	private void initDialog() {
		startDatePickerBuilder = new AlertDialog.Builder(QueryActivity.this);
		View startDatePickerView = getLayoutInflater().inflate(
			R.layout.alertitem_start_date_picker, null);
		startDatePicker = (DatePicker) startDatePickerView.findViewById(R.id.dtp_startdate);
		startDatePickerBuilder.setView(startDatePickerView);
		startDatePickerBuilder.setNegativeButton("取消", null);
		startDatePickerBuilder.setPositiveButton("确定", datePickerListener);
		startDatePickerBuilder.setTitle("请选择统计的开始时间");
		startDatePickerDialog = startDatePickerBuilder.create();
		endDatePickerBuilder = new AlertDialog.Builder(QueryActivity.this);
		View endDatePickerView = getLayoutInflater().inflate(R.layout.alertitem_end_date_picker,
			null);
		endDatePicker = (DatePicker) endDatePickerView.findViewById(R.id.dtp_enddate);
		endDatePickerBuilder.setView(endDatePickerView);
		endDatePickerBuilder.setNegativeButton("取消", null);
		endDatePickerBuilder.setPositiveButton("确定", datePickerListener);
		endDatePickerBuilder.setTitle("请选择统计的结束时间");
		endDatePickerDialog = endDatePickerBuilder.create();
		progressDialog = new ProgressDialog(this, R.style.custom_progress);
		progressDialog.setMessage("查询中...");
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.setCancelable(false);
		progressDialog.setCanceledOnTouchOutside(false);
	}

	/* 验证查询的开始,结束时间是否合理的方法 */
	private boolean validateDate() {
		if (queryStartDay.get(Calendar.YEAR) > queryEndDay.get(Calendar.YEAR)) {
			return false;
		}
		else if (queryStartDay.get(Calendar.YEAR) == queryEndDay.get(Calendar.YEAR)) {
			if (queryStartDay.get(Calendar.MONTH) > queryEndDay.get(Calendar.MONTH)) {
				return false;
			}
			else if (queryStartDay.get(Calendar.MONTH) == queryEndDay.get(Calendar.MONTH)) {
				if (queryStartDay.get(Calendar.DAY_OF_MONTH) > queryEndDay
					.get(Calendar.DAY_OF_MONTH)) {
					return false;
				}
				else return true;

			}
			else return true;

		}
		else return true;

	}

	/* 查询数据用的方法 */
	private void queryData() {

		new Thread() {
			public void run() {
				/* 根据不同的查询种类设置不同的参数 */
				TopSaleSAXHandler handler = new TopSaleSAXHandler();
				Map<String, String> params = new HashMap<String, String>();
				params.put("beginDate", edtStartDay.getText().toString());
				params.put("endDate", edtEndDay.getText().toString());
				params.put("orderBy", queryOrderByTag);
				params.put("isDesc", "1");
				/* 每次只查询10个,而且为了避免多次同时查询造成数据混乱,这里锁住了currentResultListIndex */
				synchronized (currentResultListIndex) {
					currentResultListIndex += 1;
					params.put("startRow", String.valueOf(currentResultListIndex));
					currentResultListIndex += 9;
					params.put("endRow", String.valueOf(currentResultListIndex));
					switch (queryFlag) {
					case FLAG_QUERY_WITH_DATE:
						params.put("action", "queryByCity");
						params.put("province", "-1");
						params.put("city", "-1");
						params.put("town", "-1");
						break;
					case FLAG_QUERY_WITH_SCHOOL:
						params.put("action", "queryByCity");
						params.put("province", String.valueOf(spnProvince.getSelectedItemId()));
						params.put("city", String.valueOf(spnCity.getSelectedItemId()));
						params.put("town", String.valueOf(spnCounty.getSelectedItemId()));
						break;
					case FLAG_QUERY_WITH_BOOK:
						params.put("action", "queryByBook");
						params.put("bookName", edtBookname.getText().toString());
						break;
					case FLAG_QUERY_WITH_PRESS:
						params.put("action", "queryByPress");
						params.put("press", edtPress.getText().toString());
						break;
					}
					List<TopSale> queryResultTopSales = (List<TopSale>) getWebData(params, handler);
					if (queryResultTopSales != null) {
						if (queryResultTopSales.isEmpty()) {
							mHandler.sendEmptyMessage(MSG_LOAD_FINISH);
							return;
						}
						tempDatas = queryResultTopSales;
						if (resetListFlag) {
							mHandler.sendEmptyMessage(MSG_SET_DATAS);
						}
						else {
							mHandler.sendEmptyMessage(MSG_ADD_DATAS);
						}
					}
				}
			}
		}.start();

	}

	/* 重设列表的方法 */
	protected void resetResultList() {
		queryResultAdapter.setDatas(new ArrayList<TopSale>());
		currentResultListIndex = 0;
		queryResultAdapter.notifyDataSetChanged();
		resetListFlag = true;
		lsvQueryResult.setOnScrollListener(lsvOnScrollListener);
	}

	// 选择时间edt的监听器
	private OnClickListener edtDaylistener = new View.OnClickListener() {
		public void onClick(View v) {
			/* 弹出日期的picker */
			switch (v.getId()) {
			case R.id.edt_startday:
				datePickerListener.setFromViewId(R.id.edt_startday);
				startDatePickerDialog.show();
				break;
			case R.id.edt_endday:
				datePickerListener.setFromViewId(R.id.edt_endday);
				endDatePickerDialog.show();
				break;
			}
		}
	};
	// 查询按键的监听器
	private OnClickListener btnSearchListener = new OnClickListener() {
		public void onClick(View v) {
			/* 如果查询的时间有问题的时候, 不进行查询直接返回 */
			if (queryStartDay == null) {
				Toast.makeText(getApplicationContext(), "开始时间为空, 请修改", Toast.LENGTH_SHORT).show();
				return;
			}
			if (queryEndDay == null) {
				Toast.makeText(getApplicationContext(), "结束时间为空, 请修改", Toast.LENGTH_SHORT).show();
				return;
			}
			if (!validateDate()) {
				Toast.makeText(getApplicationContext(), "开始时间必须在结束时间之前, 请修改", Toast.LENGTH_SHORT)
					.show();
				return;
			}
			progressDialog.show();
			/* 按查询按键时候重设列表的数据 */
			resetResultList();
			/* 查询数据 */
			queryData();
			/* 已经重设列表, 这里将flag设为false */
			resetListFlag = false;
		}
	};
	/* 列表滚动的监听器 */
	private OnScrollListener lsvOnScrollListener = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			/* 一发现滚动已静止并且已经都来最后一行,再查询新的数据 */
			if (scrollState == SCROLL_STATE_IDLE && (firstVisible + visibleCount == totalCount)) {
				queryData();
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
			int totalItemCount) {
			/* 将列表滚动的数据实时取出 */
			firstVisible = firstVisibleItem;
			visibleCount = visibleItemCount;
			totalCount = totalItemCount;
		}
	};
	/* 查询结果的列表的点击监听器 */
	private OnItemClickListener lsvItemClickListener = new AdapterView.OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (queryFlag == FLAG_QUERY_WITH_PRESS) {
				return;
			}
			Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
			TextView titleView = (TextView) view.findViewById(R.id.lsvitem_queryresult_titlepart);
			intent.putExtra(DetailActivity.KEY_TITLE, titleView.getText().toString());
			intent.putExtra(DetailActivity.KEY_BEGIN_DATE, edtStartDay.getText().toString());
			intent.putExtra(DetailActivity.KEY_END_DATE, edtEndDay.getText().toString());
			switch (queryFlag) {
			case FLAG_QUERY_WITH_SCHOOL:
			case FLAG_QUERY_WITH_DATE:
				intent.putExtra(DetailActivity.KEY_TYPE, DetailActivity.TYPE_SCHOOL_DETAIL);
				intent.putExtra(DetailActivity.KEY_SCHOOLID, String.valueOf(id));
				break;
			case FLAG_QUERY_WITH_BOOK:
				intent.putExtra(DetailActivity.KEY_TYPE, DetailActivity.TYPE_BOOK_DETAIL);
				intent.putExtra(DetailActivity.KEY_BOOKID, String.valueOf(id));
				break;
			}
			startActivity(intent);
		}
	};
	// 切换查询方法的监听器
	private AdapterView.OnItemSelectedListener spnStatictisKindsListener = new AdapterView.OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			// TODO Auto-generated method stub
			partArea.setVisibility(View.GONE);
			partBookname.setVisibility(View.GONE);
			partPress.setVisibility(View.GONE);
			/* 清空列表 */
			resetResultList();
			switch (position) {
			case 0:
				queryFlag = FLAG_QUERY_WITH_DATE;
				break;
			case 1:
				queryFlag = FLAG_QUERY_WITH_SCHOOL;
				partArea.setVisibility(View.VISIBLE);
				new Thread() {
					public void run() {
						Map<String, String> params = new HashMap<String, String>();
						params.put("action", "queryProvince");
						CitySAXHandler handler = new CitySAXHandler();
						List<City> cities = (List<City>) getWebData(params, handler);
						if (cities != null && !cities.isEmpty()) {
							provinceAdapter.setCities(cities);
							mHandler.sendEmptyMessage(MSG_GET_PROVINCES);
						}
					}
				}.start();
				break;
			case 2:
				queryFlag = FLAG_QUERY_WITH_BOOK;
				partBookname.setVisibility(View.VISIBLE);
				break;
			case 3:
				queryFlag = FLAG_QUERY_WITH_PRESS;
				partPress.setVisibility(View.VISIBLE);
				break;
			default:
				break;
			}
		}

		public void onNothingSelected(AdapterView<?> parent) {
		}
	};
	/* 省份spinner的选择的监听器 */
	private AdapterView.OnItemSelectedListener spnProvinceListner = new AdapterView.OnItemSelectedListener() {
		public void onItemSelected(AdapterView<?> parent, View view, int position, final long id) {
			// TODO Auto-generated method stub
			new Thread() {
				public void run() {
					Map<String, String> params = new HashMap<String, String>();
					params.put("action", "queryCity");
					params.put("pid", String.valueOf(id));
					CitySAXHandler handler = new CitySAXHandler();
					List<City> cities = (List<City>) getWebData(params, handler);
					if (cities != null) {
						cityAdapter.setCities(cities);
						mHandler.sendEmptyMessage(MSG_GET_CITIES);
						/* 有些省份的城市数据为空(港澳台),镇区的数据会保留旧的数据 ,在这里清空镇区的数据 */
						if (cities.isEmpty()) {
							countyAdapter.setCities(new ArrayList<City>());
							mHandler.sendEmptyMessage(MSG_GET_COUNTIES);
						}
					}
				}
			}.start();
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};
	/* 城市spinner的选择的监听器 */
	private AdapterView.OnItemSelectedListener spnCityListener = new AdapterView.OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> parent, View view, int posotion, final long id) {
			new Thread() {
				public void run() {
					Map<String, String> params = new HashMap<String, String>();
					params.put("action", "queryCity");
					params.put("pid", String.valueOf(id));
					CitySAXHandler handler = new CitySAXHandler();
					List<City> cities = (List<City>) getWebData(params, handler);
					if (cities != null && !cities.isEmpty()) {
						countyAdapter.setCities(cities);
						mHandler.sendEmptyMessage(MSG_GET_COUNTIES);
					}
				}
			}.start();
		}

		public void onNothingSelected(AdapterView<?> arg0) {
		}
	};

	/* 选择时间的DatePicker的监听器 */
	private class DatePickerListener implements DialogInterface.OnClickListener {
		private int fromViewId;

		public void setFromViewId(int fromViewId) {
			this.fromViewId = fromViewId;
		}

		public void onClick(DialogInterface dialog, int which) {
			switch (fromViewId) {
			case R.id.edt_startday:
				queryStartDay = GregorianCalendar.getInstance();
				queryStartDay.set(startDatePicker.getYear(), startDatePicker.getMonth(),
					startDatePicker.getDayOfMonth());
				edtStartDay.setText(CalendarTool.parseCalendarToString(queryStartDay));
				break;
			case R.id.edt_endday:
				queryEndDay = GregorianCalendar.getInstance();
				queryEndDay.set(endDatePicker.getYear(), endDatePicker.getMonth(),
					endDatePicker.getDayOfMonth());
				edtEndDay.setText(CalendarTool.parseCalendarToString(queryEndDay));
				break;
			default:
				break;
			}
		}

	}

	/* 选择排序方式的RadioGroup的监听器 */
	private OnCheckedChangeListener orderbyCheckedChangeListener = new OnCheckedChangeListener() {

		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (checkedId) {
			case R.id.rbn_orderby_money:
				queryOrderByTag = "M";
				break;
			case R.id.rbn_orderby_num:
				queryOrderByTag = "N";
				break;
			}
		}
	};

	/* 选择城市的spinner的adapter */
	private class CitysAdapter extends BaseAdapter {
		private List<City> cities;
		private City allCity;

		public void setCities(List<City> addCities) {
			cities = new ArrayList<City>();
			cities.add(allCity);
			cities.addAll(addCities);
		}

		public CitysAdapter(List<City> addCities, String tagName) {
			allCity = new City();
			allCity.setId("-1");
			allCity.setName(tagName);
			cities = new ArrayList<City>();
			cities.add(allCity);
			cities.addAll(addCities);
		}

		public int getCount() {
			return cities.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return cities.isEmpty() ? 0 : Integer.parseInt(cities.get(position).getId());
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View rootView = getLayoutInflater().inflate(R.layout.spnitem_area, null);
			TextView textView = (TextView) rootView.findViewById(R.id.txv_in_spnitem_area);
			textView.setText(cities.get(position).getName());
			return rootView;
		}
	}

	/* 查询结果列表的adapter */
	private class QueryResultAdapter extends BaseAdapter {
		private List<TopSale> datas;

		public QueryResultAdapter(List<TopSale> datas) {
			this.datas = datas;
		}

		public void setDatas(List<TopSale> datas) {
			this.datas = datas;
		}

		public void addDatas(List<TopSale> newDatas) {
			this.datas.addAll(newDatas);
		}

		public int getCount() {
			return datas.isEmpty() ? 0 : datas.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return datas.isEmpty() ? -1 : Long.parseLong(datas.get(position).getId());
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewGroup viewGroup = (ViewGroup) getLayoutInflater().inflate(
				R.layout.lsvitem_queryresult, null);
			TextView titlepartView = (TextView) viewGroup
				.findViewById(R.id.lsvitem_queryresult_titlepart);
			TextView numpartView = (TextView) viewGroup
				.findViewById(R.id.lsvitem_queryresult_numpart);
			TextView moneypartView = (TextView) viewGroup
				.findViewById(R.id.lsvitem_queryresult_moneyspart);
			TopSale topSale = datas.get(position);
			titlepartView.setText(topSale.getName());
			numpartView.setText(topSale.getNum() + "本");
			moneypartView.setText(topSale.getMoney() + "元");
			return viewGroup;
		}
	}
}
