package ovi.fh.guessthecelebbrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> celebURLs = new ArrayList<>();
    ArrayList<String> celebNames = new ArrayList<>();
    int celebChosen = 0;
    ImageView imageView;
    String[] answer = new String[4];
    int locationOfCorrectAnswer = 0;
    Button button;
    Button button2;
    Button button3;
    Button button4;

    public void buttonClicked(View view){
        if (view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))){
            Toast.makeText(getApplicationContext(), "CORRECT",Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(getApplicationContext(), "WRONG " + celebNames.get(celebChosen),Toast.LENGTH_SHORT).show();
        }
        nextQuestion();
    }

    public class ImageDownload extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection httpURLConnection =(HttpURLConnection) url.openConnection();
                httpURLConnection.connect();
                InputStream inputStream = httpURLConnection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                return bitmap;
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }
    }

    public class WebContent extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection httpURLConnection = null;

            try {
                url = new URL(urls[0]);
                httpURLConnection = (HttpURLConnection) url.openConnection();

                InputStream inputStream =  httpURLConnection.getInputStream();

                InputStreamReader reader = new InputStreamReader(inputStream);

                int data = reader.read();

                while (data != -1){
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                return result;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public void nextQuestion(){
        try {

            Random random = new Random();

            celebChosen = random.nextInt(celebURLs.size());

            ImageDownload imageDownload = new ImageDownload();

            Bitmap bitmapCeleb = imageDownload.execute(celebURLs.get(celebChosen)).get();

            imageView.setImageBitmap(bitmapCeleb);

            locationOfCorrectAnswer = random.nextInt(4);

            int incorrectAnswer;

            for (int i = 0; i < 4; i++) {
                if (i == locationOfCorrectAnswer) {
                    answer[i] = celebNames.get(celebChosen);
                } else {
                    incorrectAnswer = random.nextInt(celebURLs.size());
                    while (incorrectAnswer == celebChosen) {
                        incorrectAnswer = random.nextInt(celebURLs.size());
                    }
                    answer[i] = celebNames.get(incorrectAnswer);
                }
            }

            button.setText(answer[0]);
            button2.setText(answer[1]);
            button3.setText(answer[2]);
            button4.setText(answer[3]);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);

        button = findViewById(R.id.button);
        button2 = findViewById(R.id.button2);
        button3 = findViewById(R.id.button3);
        button4 = findViewById(R.id.button4);

        WebContent webContent = new WebContent();
        String result;
        try {
            result = webContent.execute("http://www.posh24.se/kandisar").get();

            String[] splitResult = result.split("<div class=\"listedArticles\">");

            Pattern pattern = Pattern.compile("<img src=\"(.*?)\"");
            Matcher matcher = pattern.matcher(splitResult[0]);
            while (matcher.find()){
                celebURLs.add(matcher.group(1));
            }
            pattern = Pattern.compile("alt=\"(.*?)\"");
            matcher = pattern.matcher(splitResult[0]);
            while (matcher.find()){
                celebNames.add(matcher.group(1));
            }

            nextQuestion();
            Log.i("Result",result);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
