package idu.stenden.inf1i.homewizard;

// TODO: Clean up imports here

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

public class BaseMqttEventActivity extends AppCompatActivity {

    protected ArrayList<MqttControllerMessageCallbackListener> listeners = new ArrayList<>();
    //protected boolean eventHandlersAdded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

            removeEventListeners();
            addEventListeners();
    }

    protected void addEventListener(MqttControllerMessageCallbackListener listener){
        if(MqttController.getInstance().hasMessageListener(listener)){
            MqttController.getInstance().removeMessageListener(listener);
        }
        listeners.add(listener);
        MqttController.getInstance().addMessageListener(listener);
    }
	
	/// Override this with your event handlers
	protected void addEventListeners(){
		
	}

    /// Should remove all of those event handlers
    protected void removeEventListeners(){
        for(MqttControllerMessageCallbackListener m : listeners){
            MqttController.getInstance().removeMessageListener(m);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        removeEventListeners();
    }
}
