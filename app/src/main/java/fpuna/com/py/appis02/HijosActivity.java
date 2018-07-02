package fpuna.com.py.appis02;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class HijosActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener{

    private LinearLayout Prof_Section;
    private Button SignOut;
    private TextView Name, Email;
    private ImageView Prof_pic;
    private GoogleApiClient googleApiClient;
    private static final int REQ_CODE = 9001;

    private RecuperarHijos recuperarHijos = null;
    private RecuperarVacunaciones recuperarVacunaciones = null;
    private String resultado, resultadoVac = "";
    private int pUsuarioId = 0;
    private int pHijoId = 0;
    private TableLayout tablaHijos;
    JSONArray jsonArray, jsonArrayVac;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hijos);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Prof_Section = (LinearLayout)findViewById(R.id.prof_section);
        SignOut = (Button)findViewById(R.id.btn_logout_);
        Name = (TextView) findViewById(R.id.name_);
        Email = (TextView) findViewById(R.id.email_);
        Prof_pic = (ImageView)findViewById(R.id.prof_pic_);
        SignOut.setOnClickListener(this);
        Bundle datos = this.getIntent().getExtras();
        Name.setText(datos.getString("name"));
        Email.setText(datos.getString("email"));
        pUsuarioId = datos.getInt("usuarioId");
        Glide.with(this).load(datos.getString("foto")).into(Prof_pic);
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this,this).addApi(Auth.GOOGLE_SIGN_IN_API,signInOptions).build();

        tablaHijos = (TableLayout) findViewById(R.id.tabla_hijos);
        recuperarHijos = new RecuperarHijos();
        recuperarHijos.execute();

    }

    protected void cargarTablaHijos(){
        if(tablaHijos.getChildCount() > 1){
            int filas = tablaHijos.getChildCount();

            tablaHijos.removeViews(1, filas-1);
        }

        Integer count=0;
        // Create the table row
        if(jsonArray.length() != 0){
            for (int i=0; i<jsonArray.length(); i++){
                try {
                    JSONObject json = jsonArray.getJSONObject(i);

                    TableRow tr = new TableRow(this);
                    //if(count%2!=0){ tr.setBackgroundColor(Color.WHITE);}else{tr.setBackgroundColor(Color.GREEN);};
                    tr.setId(100+count);
                    tr.setLayoutParams(new TableLayout.LayoutParams(
                            TableRow.LayoutParams.FILL_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT));
                    tr.setOnClickListener( new View.OnClickListener() {
                        @Override
                        public void onClick( View v ) {
                            TableRow t = (TableRow) v;
                            TextView firstTextView = (TextView) t.getChildAt(0);
                            TextView secondTextView = (TextView) t.getChildAt(1);
                            TextView thirdTextView = (TextView) t.getChildAt(2);
                            TextView forthTextView = (TextView) t.getChildAt(3);
                            Integer firstText = Integer.parseInt(firstTextView.getText().toString());
                            String secondText = secondTextView.getText().toString();
                            String thirdText = thirdTextView.getText().toString();
                            String forthText = forthTextView.getText().toString();
                            //Toast.makeText(getApplicationContext() , "clic en row "+ firstText, Toast.LENGTH_LONG).show();

                            Intent in = new Intent(HijosActivity.this,VacunasActivity.class);
                            int idHijo = 0;
                            in.putExtra("hijoId", firstText);
                            in.putExtra("nombre", thirdText + " " + forthText);
                            in.putExtra("cedula", secondText);
                            startActivity(in);
                        }
                    } );
                    //Create columns to add as table data
                    TextView textId = new TextView(this);
                    textId.setId(200+count);
                    textId.setText(json.getString("idHijo"));
                    textId.setPadding(2, 0, 5, 0);
                    textId.setTextColor(Color.BLACK);
                    tr.addView(textId);
                    // Create a TextView to add date
                    TextView textCedula = new TextView(this);
                    textCedula.setId(200+count);
                    textCedula.setText(json.getString("cedula"));
                    textCedula.setPadding(2, 0, 5, 0);
                    textCedula.setTextColor(Color.BLACK);
                    tr.addView(textCedula);

                    TextView textNombre = new TextView(this);
                    textNombre.setId(200+count);
                    textNombre.setText(json.getString("nombre"));
                    textNombre.setPadding(2, 0, 5, 0);
                    textNombre.setTextColor(Color.BLACK);
                    tr.addView(textNombre);

                    TextView textApellido = new TextView(this);
                    textApellido.setId(200+count);
                    textApellido.setText(json.getString("apellido"));
                    textApellido.setPadding(2, 0, 5, 0);
                    textApellido.setTextColor(Color.BLACK);
                    tr.addView(textApellido);

                    SimpleDateFormat parseador = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat formateador = new SimpleDateFormat("dd/MM/yyyy");

                    System.out.println("Groot " + json.getString("fechaNacimiento"));
                    Date date = parseador.parse(json.getString("fechaNacimiento"));
                    TextView textNacimiento = new TextView(this);
                    textNacimiento.setId(200+count);
                    textNacimiento.setText(formateador.format(date));
                    textNacimiento.setPadding(2, 0, 5, 0);
                    textNacimiento.setTextColor(Color.BLACK);
                    tr.addView(textNacimiento);

                    TextView textSexo = new TextView(this);
                    textSexo.setId(200+count);
                    textSexo.setText(json.getInt("sexo") == 0? "M" : "F");
                    textSexo.setPadding(2, 0, 5, 0);
                    textSexo.setTextColor(Color.BLACK);
                    tr.addView(textSexo);

                    // finally add this to the table row
                    tablaHijos.addView(tr, new TableLayout.LayoutParams(
                            TableRow.LayoutParams.FILL_PARENT,
                            TableRow.LayoutParams.WRAP_CONTENT));
                    count++;
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }else{
            Toast.makeText(getApplicationContext() , "No tiene hijos.", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onClick(View view) {
        new MainActivity().signOut();
    }

    public void signOut(){
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                Intent i = new Intent(HijosActivity.this,MainActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void mostrarNotificacion() {
        if(jsonArray.length() != 0){
            for (int i=0; i<jsonArray.length(); i++){
                try {
                    JSONObject json = jsonArray.getJSONObject(i);
                    pHijoId = Integer.parseInt(json.getString("idHijo"));
                    recuperarVacunaciones = new RecuperarVacunaciones();
                    recuperarVacunaciones.execute();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void notificarVacuna(){
        Integer firstText = 0;
        String notificaciones_detalle = "", secondText = "", thirdText = "", forthText = "";
        if(jsonArrayVac.length() != 0){
            Date hoy = new Date();
            for (int i=0; i<jsonArrayVac.length(); i++){
                try {
                    JSONObject json = jsonArrayVac.getJSONObject(i);
                    JSONObject jsonVacuna = json.getJSONObject("idVacuna");
                    JSONObject jsonHijo = json.getJSONObject("idHijo");

                    SimpleDateFormat parseador = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat formateador = new SimpleDateFormat("dd/MM/yyyy");

                    Date fecha_vac = parseador.parse(json.getString("fecha"));
                    System.out.println("Vision "+json.getString("fecha"));
                    System.out.println("Wanda "+json.getInt("estado"));
                    System.out.println("forever  "+hoy);
                    if(json.getInt("estado") == 1 && hoy.getYear() == fecha_vac.getYear() && hoy.getMonth() == fecha_vac.getMonth() && (hoy.getDate()+2) >= fecha_vac.getDate()){
                        firstText = jsonHijo.getInt("idHijo");
                        secondText = jsonHijo.getString("cedula");
                        thirdText = jsonHijo.getString("nombre");
                        forthText = jsonHijo.getString("apellido");
                        notificaciones_detalle = notificaciones_detalle + "Su hij@ " + jsonHijo.getString("nombre") + " " + jsonHijo.getString("apellido") + " debe vacunarse contra la " + jsonVacuna.getString("nombre") + " en fecha " +formateador.format(fecha_vac) + "\n";

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if(!notificaciones_detalle.equals("")){
                initChannels(this);

                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(this,"default")
                                .setSmallIcon(R.mipmap.ic_launcher)
                                //.setLargeIcon((((BitmapDrawable)getResources()
                                     //   .getDrawable(R.mipmap.ic_launcher)).getBitmap()))
                                .setContentTitle("Aviso de vacunación")
                                .setContentText(notificaciones_detalle)
                                .setTicker("Aviso de vacunación");

                Intent notificationIntent = new Intent(this, VacunasActivity.class);
                notificationIntent.putExtra("hijoId", firstText);
                notificationIntent.putExtra("nombre", thirdText + " " + forthText);
                notificationIntent.putExtra("cedula", secondText);
                PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(contentIntent);

                // Add as notification
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancelAll();
                manager.notify(0, builder.build());

                
            }

        }

    }

    public void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("default",
                "Channel name",
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Channel description");
        notificationManager.createNotificationChannel(channel);
    }

    private class RecuperarHijos extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://10.0.2.2:8080/AppRestIS2WS/services/usuarios/consultaHijos");
            post.setHeader("content-type", "application/json");
            try {
                JSONObject dato = new JSONObject();
                dato.put("idUsuario",pUsuarioId);
                StringEntity entity = new StringEntity(dato.toString());
                post.setEntity(entity);
                HttpResponse resp = httpClient.execute(post);
                resultado = EntityUtils.toString(resp.getEntity());
            }catch (Exception ex){
                ex.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if(!success){
                Toast.makeText(getApplicationContext() , "Error: no se recuperaron hijos para este usuario", Toast.LENGTH_LONG).show();
                cargarTablaHijos();
            }
            else{
                try {
                        System.out.println("Batman "+resultado);
                        jsonArray = new JSONArray(resultado);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                cargarTablaHijos();
                mostrarNotificacion();
            }
        }

        @Override
        protected void onCancelled() {
            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
        }
    }

    private class RecuperarVacunaciones extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://10.0.2.2:8080/AppRestIS2WS/services/registros/consultar_por_hijo/"+pHijoId);
            try {

                HttpResponse resp = httpClient.execute(post);
                resultadoVac = EntityUtils.toString(resp.getEntity());
            }catch (Exception ex){
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if(!success){

            }
            else{
                try {
                    jsonArrayVac = new JSONArray(resultadoVac);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                notificarVacuna();
            }
        }

        @Override
        protected void onCancelled() {
            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
        }
    }
}
