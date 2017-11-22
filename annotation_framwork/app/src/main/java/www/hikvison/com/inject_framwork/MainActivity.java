package www.hikvison.com.inject_framwork;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.hikvision.www.InjectException.InjectExceptionAnnotation;
import com.hikvision.www.framwork.InjectView;
import com.hikvision.www.framwork.Unbinder;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";

    @InjectExceptionAnnotation(annotationClass = CatchHandler.class
            , catchPath = "a")
    protected CatchHandler catchHandler;

    @InjectExceptionAnnotation(annotationClass = Test.class
            , catchPath = "a")
    protected Test test;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Unbinder unbinder = InjectView.inject(this);
        catchHandler.say();
        String name = test.getName("hello injectView");
        Log.e(TAG, "onCreate: " + name);
        unbinder.unbind();
    }
}
