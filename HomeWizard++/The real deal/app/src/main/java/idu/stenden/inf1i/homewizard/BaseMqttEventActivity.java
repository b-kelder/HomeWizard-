package idu.stenden.inf1i.homewizard;

// TODO: Clean up imports here

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

public class BaseMqttEventActivity extends AppCompatActivity {

    protected ArrayList<MqttControllerMessageCallbackListener> eventHandlers = new ArrayList<>();
    //protected boolean eventHandlersAdded = false;

    @Override
    protected void onStart() {

        super.onStart();
        
        //if(!eventHandlersAdded) {
			//eventHandlersAdded = true;
            //Toast.makeText(getApplicationContext(), "Added event handlers", Toast.LENGTH_SHORT).show();
            addEventListeners();
		//}
    }

    protected void addEventListener(MqttControllerMessageCallbackListener listener){
        if(MqttController.getInstance().hasMessageListener(listener)){
            MqttController.getInstance().removeMessageListener(listener);
        }
        eventHandlers.add(listener);
        MqttController.getInstance().addMessageListener(listener);
    }
	
	/// Override this with your event handlers
	protected void addEventListeners(){
		
	}

    /// Should remove all of those event handlers
    protected void removeEventHandlers(){
        for(MqttControllerMessageCallbackListener m : eventHandlers){
            MqttController.getInstance().removeMessageListener(m);
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        removeEventHandlers();
    }
}
