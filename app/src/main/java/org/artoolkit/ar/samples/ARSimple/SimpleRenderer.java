/*
 *  SimpleRenderer.java
 *  ARToolKit5
 *
 *  Disclaimer: IMPORTANT:  This Daqri software is supplied to you by Daqri
 *  LLC ("Daqri") in consideration of your agreement to the following
 *  terms, and your use, installation, modification or redistribution of
 *  this Daqri software constitutes acceptance of these terms.  If you do
 *  not agree with these terms, please do not use, install, modify or
 *  redistribute this Daqri software.
 *
 *  In consideration of your agreement to abide by the following terms, and
 *  subject to these terms, Daqri grants you a personal, non-exclusive
 *  license, under Daqri's copyrights in this original Daqri software (the
 *  "Daqri Software"), to use, reproduce, modify and redistribute the Daqri
 *  Software, with or without modifications, in source and/or binary forms;
 *  provided that if you redistribute the Daqri Software in its entirety and
 *  without modifications, you must retain this notice and the following
 *  text and disclaimers in all such redistributions of the Daqri Software.
 *  Neither the name, trademarks, service marks or logos of Daqri LLC may
 *  be used to endorse or promote products derived from the Daqri Software
 *  without specific prior written permission from Daqri.  Except as
 *  expressly stated in this notice, no other rights or licenses, express or
 *  implied, are granted by Daqri herein, including but not limited to any
 *  patent rights that may be infringed by your derivative works or by other
 *  works in which the Daqri Software may be incorporated.
 *
 *  The Daqri Software is provided by Daqri on an "AS IS" basis.  DAQRI
 *  MAKES NO WARRANTIES, EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
 *  THE IMPLIED WARRANTIES OF NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS
 *  FOR A PARTICULAR PURPOSE, REGARDING THE DAQRI SOFTWARE OR ITS USE AND
 *  OPERATION ALONE OR IN COMBINATION WITH YOUR PRODUCTS.
 *
 *  IN NO EVENT SHALL DAQRI BE LIABLE FOR ANY SPECIAL, INDIRECT, INCIDENTAL
 *  OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) ARISING IN ANY WAY OUT OF THE USE, REPRODUCTION,
 *  MODIFICATION AND/OR DISTRIBUTION OF THE DAQRI SOFTWARE, HOWEVER CAUSED
 *  AND WHETHER UNDER THEORY OF CONTRACT, TORT (INCLUDING NEGLIGENCE),
 *  STRICT LIABILITY OR OTHERWISE, EVEN IF DAQRI HAS BEEN ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 *
 *  Copyright 2015 Daqri, LLC.
 *  Copyright 2011-2015 ARToolworks, Inc.
 *
 *  Author(s): Julian Looser, Philip Lamb
 *
 */

package org.artoolkit.ar.samples.ARSimple;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.renderscript.Matrix4f;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.artoolkit.ar.base.ARToolKit;
import org.artoolkit.ar.base.rendering.ARRenderer;
import org.artoolkit.ar.base.rendering.Cube;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import static org.artoolkit.ar.samples.ARSimple.ARSimple.deviceId;
import static org.artoolkit.ar.samples.ARSimple.ARSimple.mRotationMatrix;


/**
 * A very simple Renderer that adds a marker and draws a cube on it.
 */
public class SimpleRenderer extends ARRenderer {

//	public float[] inertialm = new float[16];

	private int markerID = -1;

	private Cube cube = new Cube(40.0f, 0.0f, 0.0f, 20.0f);
	private float angle = 0.0f;
	private boolean spinning = false;

	private long postedTimestamp = System.currentTimeMillis();
	@Override
	public boolean configureARScene() {

		markerID = ARToolKit.getInstance().addMarker("single;Data/patt.hiro;80");

		if (markerID < 0) return false;

		return true;
	}


	private class PostTask extends AsyncTask<String, String, Void> {
		@Override
		protected Void doInBackground(String... data) {
			// Create a new HttpClient and Post Header
			HttpClient httpclient = new DefaultHttpClient();

			HttpPost httppost = new HttpPost("http://128.237.195.18:5000/devicetrack");

			try {
				//add data
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
				nameValuePairs.add(new BasicNameValuePair("data", data[0]));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				//execute http post
				HttpResponse response = httpclient.execute(httppost);


			} catch (IOException e) {
				Log.e("e",e.toString());
			}
			return null;
		}
	}

	public void click() {
		spinning = !spinning;
	}

	public void draw(GL10 gl) {

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		gl.glMatrixMode(GL10.GL_PROJECTION);

		gl.glLoadMatrixf(ARToolKit.getInstance().getProjectionMatrix(), 0);
		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glFrontFace(GL10.GL_CW);

		gl.glMatrixMode(GL10.GL_MODELVIEW);

		if (ARToolKit.getInstance().queryMarkerVisible(markerID)) {

			gl.glLoadMatrixf(ARToolKit.getInstance().queryMarkerTransformation(markerID), 0);

			float[] markerTransformation = ARToolKit.getInstance().queryMarkerTransformation(markerID);

			Matrix4f img_matrix = new Matrix4f(markerTransformation);

			Matrix4f res_matrix = new Matrix4f(mRotationMatrix);
			img_matrix.inverse();
			img_matrix.multiply(res_matrix);
			Log.d("GAP", "" + img_matrix.getArray().length + " " + Arrays.toString(img_matrix.getArray()) );
			float [] new_matrix = img_matrix.getArray();
			float x = new_matrix[12]/10;
			float y = new_matrix[13]/10;
			float z = new_matrix[14]/10;
//			float x = markerTransformation[12]/10;
//			float y = markerTransformation[13]/10;
//			float z = markerTransformation[14]/10;
			Log.e("matrix", Arrays.toString(markerTransformation)+"");
			String data = x+","+y+","+z + ","+deviceId;


			if(System.currentTimeMillis()-postedTimestamp>100){
				Log.e("data",data);
				new PostTask().execute(data);
				postedTimestamp = System.currentTimeMillis();
			}

			gl.glPushMatrix();
			gl.glRotatef(angle, 0.0f, 0.0f, 1.0f);
			cube.draw(gl);
			gl.glPopMatrix();

			if (spinning) angle += 5.0f;
		}


	}
}