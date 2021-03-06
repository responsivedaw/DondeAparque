package com.jldes.dondeaparque;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class Mapa extends android.support.v4.app.FragmentActivity implements
		LocationListener {
	private GoogleMap mapa = null;
	private MarkerOptions coche;
	private MarkerOptions yo;
	private LocationManager locationManager;
	private boolean condition = true;
	static double lat;
	static double lon;
	static double lat2;
	static double lon2;
	private DrawerLayout drawerLayout;
	ListView navListView;
	private String[] opcionesMenu;
	private ActionBarDrawerToggle drawerToggle;

	@Override
	protected void onCreate(Bundle saveInstanceState) {
		super.onCreate(saveInstanceState);
		setContentView(R.layout.activity_mapas);
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		navListView = (ListView) findViewById(R.id.left_drawer);
		opcionesMenu = new String[] { "Ayuda", "Puntuar" };
		navListView.setAdapter(new ArrayAdapter<String>(getActionBar()
				.getThemedContext(), android.R.layout.simple_list_item_1,
				opcionesMenu));
		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
				R.drawable.ic_navigation_drawer, R.string.app_name,
				R.string.app_name) {

			public void onDrawerClosed(View view) {
				ActivityCompat.invalidateOptionsMenu(Mapa.this);
			}

			public void onDrawerOpened(View drawerView) {
				ActivityCompat.invalidateOptionsMenu(Mapa.this);
			}
		};

		drawerLayout.setDrawerListener(drawerToggle);
		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setBackgroundDrawable(
				new ColorDrawable(Color.parseColor("#30898e")));
		getActionBar().setIcon(getResources().getDrawable(R.drawable.titulo));
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		navListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {

				view.setSelected(true);
				switch (pos) {
				case 0:

					Intent intent = new Intent(Mapa.this, Ayuda.class);
					startActivity(intent);
					break;
				case 1:

					startActivity(new Intent(
							Intent.ACTION_VIEW,
							Uri.parse("https://play.google.com/store/apps/details?id=com.jldes.dondeaparque")));
					break;

				}
				drawerLayout.closeDrawer(navListView);

			}
		});
		ImageView imageView = (ImageView) findViewById(R.id.boton_pos);
		imageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CameraPosition campos2 = new CameraPosition(new LatLng(lat2,
						lon2), 18, 0, 0);
				CameraUpdate camUpd2 = CameraUpdateFactory
						.newCameraPosition(campos2);
				mapa.animateCamera(camUpd2);
			}
		});
		comprovarconexion();
		empezar();
	}

	private void comprovarconexion() {
		AlertDialog.Builder dialogo3 = new AlertDialog.Builder(this);
		dialogo3.setMessage(
				"Comprueva tu conexi�n de datos y vuelve a intentarlo")
				.setTitle("Sin conexi�n de red")
				.setPositiveButton("Volver a intentar",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								comprovarconexion();
							}
						})
				.setNegativeButton("Salir",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								locationManager.removeUpdates(Mapa.this);
								finish();
								dialog.cancel();
							}
						});

		AlertDialog alertDialog = dialogo3.create();
		if (!isOnline()) {
			alertDialog.show();
		}
	}

	public boolean isOnline() {
		Context context = getApplicationContext();
		ConnectivityManager connectMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectMgr != null) {
			NetworkInfo[] netInfo = connectMgr.getAllNetworkInfo();
			if (netInfo != null) {
				for (NetworkInfo net : netInfo) {
					if (net.getState() == NetworkInfo.State.CONNECTED) {
						Log.d("Red", "Si");
						return true;
					}
				}
			}
		} else {
			Log.d("NETWORK", "No network available");
		}
		Log.d("Red", "No");
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		menu.findItem(R.id.menu_coche).getIcon()
				.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (drawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		switch (item.getItemId()) {
		case R.id.compartir:
			Geocoder geocoder = new Geocoder(Mapa.this, Locale.getDefault());
			try {
				List<Address> addresses = geocoder.getFromLocation(
						coche.getPosition().latitude,
						coche.getPosition().longitude, 1);
				if (addresses.size() > 0) {
					Social.share(this,
							getResources().getString(R.string.app_name),
							addresses.get(0).getAddressLine(0));
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case R.id.menu_coche:
			CameraPosition camPos3 = new CameraPosition(coche.getPosition(),
					18, 0, 0);
			mapa.animateCamera(CameraUpdateFactory.newCameraPosition(camPos3));
			break;
		case R.id.guardar:
			showDialog(0);
			break;
		case R.id.ayuda:
			startActivity(new Intent(Mapa.this, Ayuda.class));
			break;
		case R.id.puntuar:
			startActivity(new Intent(
					Intent.ACTION_VIEW,
					Uri.parse("https://play.google.com/store/apps/details?id=com.jldes.dondeaparque")));

			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		locationManager.removeUpdates(this);
		super.onDestroy();
	}

	private void empezar() {
		SharedPreferences preferences = getSharedPreferences("opciones",
				MODE_PRIVATE);
		lat = preferences.getFloat("latitud", 0);
		lon = preferences.getFloat("longitud", 0);
		coche = new MarkerOptions()
				.position(new LatLng(lat, lon))
				.draggable(true)
				.title("Coche")
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.indicador_coche));
		mapa = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();
		Marker marcador_coche = mapa.addMarker(coche);
		mapa.setOnMarkerDragListener(new OnMarkerDragListener() {

			@Override
			public void onMarkerDragStart(Marker marcador) {
			}

			@Override
			public void onMarkerDragEnd(Marker marcador) {
				coche.position(marcador.getPosition());
				SharedPreferences preferences = getSharedPreferences(
						"opciones", MODE_PRIVATE);
				SharedPreferences.Editor editor = preferences.edit();
				editor.putBoolean("habil", true);
				editor.putFloat("latitud",
						(float) marcador.getPosition().latitude);
				editor.putFloat("longitud",
						(float) marcador.getPosition().longitude);
				editor.commit();
			}

			@Override
			public void onMarkerDrag(Marker marcador) {
				coche.position(marcador.getPosition());
				SharedPreferences preferences = getSharedPreferences(
						"opciones", MODE_PRIVATE);
				SharedPreferences.Editor editor = preferences.edit();
				editor.putBoolean("habil", true);
				editor.putFloat("latitud",
						(float) marcador.getPosition().latitude);
				editor.putFloat("longitud",
						(float) marcador.getPosition().longitude);
				editor.commit();
			}
		});
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 0, 0, this);
		final Location loc = locationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		yo = new MarkerOptions().title("Yo").icon(
				BitmapDescriptorFactory
						.fromResource(R.drawable.indicador_persona));
		final Handler handler = new Handler();
		final Runnable runnable = new Runnable() {

			@Override
			public void run() {
				actualizarposicion(loc);
			}
		};
		Thread tiempo = new Thread() {
			@Override
			public void run() {
				try {
					handler.post(runnable);
				} catch (Exception e) {
				}
			}
		};
		tiempo.start();
	}

	private void mostrarMarcador(double lat, double lng, int i) {
		switch (i) {
		case 0:
			mapa.addMarker(coche);
			break;

		case 1:
			mapa.addMarker(yo);
			break;
		}
	}

	public Dialog onCreateDialog(int id) {
		// Use the Builder class for convenient dialog construction

		Dialog dialog = null;
		switch (id) {
		case 0:
			AlertDialog.Builder dialogo0 = new AlertDialog.Builder(this);
			dialogo0.setMessage(
					"�SEGURO QUE QUIERES BORRAR LA POSICI�N ACTUAL?")
					.setPositiveButton("S�",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									SharedPreferences preferences = getSharedPreferences(
											"opciones", MODE_PRIVATE);
									SharedPreferences.Editor editor = preferences
											.edit();
									editor.putBoolean("habil", false);
									editor.commit();
									startActivity(new Intent(Mapa.this,
											MainActivity.class));
									finish();

								}
							})
					.setNegativeButton("NO",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
								}
							});
			dialog = dialogo0.create();
			// Create the AlertDialog object and return i
			break;
		case 1:
			AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
			dialogo1.setMessage("�QUIERES SALIR?")
					.setPositiveButton("S�",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									locationManager.removeUpdates(Mapa.this);
									finish();
								}
							})
					.setNegativeButton("NO",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
								}
							});
			dialog = dialogo1.create();
			// Create the AlertDialog object and return i
			break;
		case 2:
			AlertDialog.Builder dialogo2 = new AlertDialog.Builder(this);
			dialogo2.setMessage("NO SE PUEDE ENCONTRAR LA POSICI�N")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			dialog = dialogo2.create();
			break;
		case 3:
			AlertDialog.Builder dialogo3 = new AlertDialog.Builder(this);
			dialogo3.setMessage("�Usar Wi-Fi para obtener posici�n?")
					.setTitle("SIN DATOS WI-FI")
					.setPositiveButton("Aceptar",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Intent actividad = new Intent(
											Settings.ACTION_LOCATION_SOURCE_SETTINGS);
									startActivity(actividad);
									dialog.cancel();
								}
							});
			dialog = dialogo3.create();
			break;

		}

		return dialog;
	}

	public void actualizarposicion(Location location) {
		if (location != null) {
			lat2 = location.getLatitude();
			lon2 = location.getLongitude();
			mapa.clear();
			mapa.addMarker(coche);
			yo.position(new LatLng(lat2, lon2));
			mapa.addMarker(yo);
			// ruta();
			if (condition) {
				condition = !condition;
				CameraPosition campos2 = new CameraPosition(new LatLng(lat2,
						lon2), 18, 60, location.getBearing());
				CameraUpdate camUpd2 = CameraUpdateFactory
						.newCameraPosition(campos2);
				mapa.animateCamera(camUpd2);
			}

		} else {
			showDialog(2);
		}
	}

	private void mostrarRuta(LatLng inicio, LatLng fin) {
		PolylineOptions lineas = new PolylineOptions().add(inicio).add(fin);

		lineas.width(8);
		lineas.color(Color.parseColor("#84C225"));

		mapa.addPolyline(lineas);

	}

	public void onBackPressed() {
		showDialog(1);
	}

	private void ruta() {
		JSONObject json = this.rutaEntreDosPuntos();
		try {
			ArrayList<LatLng> puntosRuta = new ArrayList<LatLng>();
			JSONArray ruta = json.getJSONArray("routes").getJSONObject(0)
					.getJSONArray("legs").getJSONObject(0)
					.getJSONArray("steps");

			int numTramos = ruta.length();
			LatLng inicio;
			LatLng fin;
			for (int i = 0; i < numTramos; i++) {
				String puntosCodificados = ruta.getJSONObject(i)
						.getJSONObject("polyline").getString("points");
				ArrayList<LatLng> puntosTramo = obtenPuntosTramo(puntosCodificados);
				puntosRuta.addAll(puntosTramo);
				inicio = new LatLng(Double.parseDouble(ruta.getJSONObject(i)
						.getJSONObject("start_location").getString("lat")),
						Double.parseDouble(ruta.getJSONObject(i)
								.getJSONObject("start_location")
								.getString("lng")));
				fin = new LatLng(
						Double.parseDouble(ruta.getJSONObject(i)
								.getJSONObject("end_location").getString("lat")),
						Double.parseDouble(ruta.getJSONObject(i)
								.getJSONObject("end_location").getString("lng")));
				mostrarRuta(inicio, fin);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	private ArrayList<LatLng> obtenPuntosTramo(String puntosCodificados) {
		ArrayList<LatLng> puntosDecodificados = new ArrayList<LatLng>();
		puntosCodificados = puntosCodificados.replace("\\\\",
				String.valueOf('\\'));

		int i = 0;
		int latitud = 0;
		int longitud = 0;
		while (i < puntosCodificados.length()) {
			int c;
			int desplazamiento = 0;
			int resultado = 0;
			do {
				c = puntosCodificados.charAt(i++) - 63;
				resultado |= (c & 0x1f) << desplazamiento;
				desplazamiento += 5;
			} while (c >= 0x20);
			int auxLat = ((resultado & 1) != 0 ? ~(resultado >> 1)
					: (resultado >> 1));
			latitud += auxLat;

			desplazamiento = 0;
			resultado = 0;
			do {
				c = puntosCodificados.charAt(i++) - 63;
				resultado |= (c & 0x1f) << desplazamiento;
				desplazamiento += 5;
			} while (c >= 0x20);
			int auxLng = ((resultado & 1) != 0 ? ~(resultado >> 1)
					: (resultado >> 1));
			longitud += auxLng;

			LatLng p = new LatLng((int) (((double) latitud / 1E5) * 1E6),
					(int) (((double) longitud / 1E5) * 1E6));
			puntosDecodificados.add(p);
		}
		return puntosDecodificados;
	}

	private JSONObject rutaEntreDosPuntos() {
		String url = "http://maps.google.com/maps/api/directions/json?origin=";
		url = url.concat(String.valueOf(lat2));
		url = url.concat(",");
		url = url.concat(String.valueOf(lon2));
		url = url.concat("&destination=");
		url = url.concat(String.valueOf(lat));
		url = url.concat(",");
		url = url.concat(String.valueOf(lon));
		url = url.concat("&mode=walking&unitsystem=metric&sensor=false");

		HttpGet httpGet = new HttpGet(url);
		HttpClient cliente = new DefaultHttpClient();
		HttpResponse respuesta;
		StringBuilder cons = new StringBuilder();
		try {
			respuesta = cliente.execute(httpGet);
			HttpEntity entidad = respuesta.getEntity();
			InputStream stream = entidad.getContent();
			int i;
			while ((i = stream.read()) != -1) {
				cons.append((char) i);
			}
		} catch (ClientProtocolException e) {
		} catch (IOException e) {
		}

		JSONObject json = new JSONObject();
		try {
			json = new JSONObject(cons.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

	@Override
	public void onLocationChanged(Location location) {
		comprovarconexion();
		actualizarposicion(location);
	}

	@Override
	public void onProviderDisabled(String provider) {
		showDialog(3);
	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this); 
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this); 
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		boolean menuAbierto = drawerLayout.isDrawerOpen(navListView);

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}
}