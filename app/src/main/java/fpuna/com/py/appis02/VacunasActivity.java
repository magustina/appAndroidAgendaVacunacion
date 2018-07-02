package fpuna.com.py.appis02;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

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

/**
 * Created by pc on 17/12/2017.
 */

public class VacunasActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView Nombre, Cedula;
    private int pHijoId = 0;


    private RecuperarVacunaciones recuperarVacunaciones = null;
    private String resultado = "";
    private TableLayout tablaVacunaciones;
    JSONArray jsonArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vacunas_activity);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Nombre = (TextView) findViewById(R.id.nombre);
        Cedula = (TextView) findViewById(R.id.cedula);

        Bundle datos = this.getIntent().getExtras();
        pHijoId = datos.getInt("hijoId");
        Nombre.setText(datos.getString("nombre"));
        Cedula.setText(datos.getString("cedula"));

        tablaVacunaciones = (TableLayout) findViewById(R.id.tabla_vacunas);
        recuperarVacunaciones = new RecuperarVacunaciones();
        recuperarVacunaciones.execute();
        //addNotification();
    }

    @Override
    public void onClick(View view) {

    }

    private void addNotification() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Notifications Example")
                        .setContentText("This is a test notification");

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(0);
        manager.notify(0, builder.build());
    }

    protected void cargarTablaVacunaciones(){
        if(tablaVacunaciones.getChildCount() > 1){
            int filas = tablaVacunaciones.getChildCount();

            tablaVacunaciones.removeViews(1, filas-1);
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

                    //Create columns to add as table data
                    JSONObject jsonVacuna = json.getJSONObject("idVacuna");

                    TextView textNombreVac = new TextView(this);
                    textNombreVac.setId(200+count);
                    textNombreVac.setText(jsonVacuna.getString("nombre"));
                    textNombreVac.setPadding(4, 0, 4, 0);
                    textNombreVac.setTextColor(Color.BLACK);
                    tr.addView(textNombreVac);

                    SimpleDateFormat parseador = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat formateador = new SimpleDateFormat("dd/MM/yyyy");

                    Date date = parseador.parse(json.getString("fecha"));
                    TextView textFechaVac = new TextView(this);
                    textFechaVac.setId(200+count);
                    textFechaVac.setText(formateador.format(date));
                    textFechaVac.setPadding(5, 0, 2, 0);
                    textFechaVac.setTextColor(Color.BLACK);
                    tr.addView(textFechaVac);

                    TextView textEstado = new TextView(this);
                    textEstado.setId(200+count);
                    textEstado.setText(json.getInt("estado") == 0? "NO" : "SI");
                    textEstado.setPadding(3, 0, 3, 0);
                    textEstado.setTextColor(Color.BLACK);
                    tr.addView(textEstado);

                    // finally add this to the table row
                    tablaVacunaciones.addView(tr, new TableLayout.LayoutParams(
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
            Toast.makeText(getApplicationContext() , "No se recuperaron vacunaciones para este hijo.", Toast.LENGTH_LONG).show();
        }

    }

    private class RecuperarVacunaciones extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost post = new HttpPost("http://10.0.2.2:8080/AppRestIS2WS/services/registros/consultar_por_hijo/"+pHijoId);
            try {

                HttpResponse resp = httpClient.execute(post);
                resultado = EntityUtils.toString(resp.getEntity());
            }catch (Exception ex){
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if(!success){
                Toast.makeText(getApplicationContext() , "Error: no se recuperaron vacunaciones para este hijo", Toast.LENGTH_LONG).show();
                cargarTablaVacunaciones();
            }
            else{
                try {
                    jsonArray = new JSONArray(resultado);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                cargarTablaVacunaciones();
            }
        }

        @Override
        protected void onCancelled() {
            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
        }
    }

}
