package com.example.musicnotespractice.utils

/**
 * @author Joren Six
 * An implementation of the YIN pitch tracking algorithm.
 * See [the YIN paper.](http://recherche.ircam.fr/equipes/pcm/cheveign/ps/2002_JASA_YIN_proof.pdf)
 *
 * Implementation originally based on [aubio](http://aubio.org)
 *
 *
 * Updated by Emlyn O'Regan to work in the PitchDetect sample project for Android.
 * I removed all the realtime features (which are tied in with javax libraries, not good for Dalvik), and
 * modified Yin to be called with a byte buffer to be analyzed using the getPitch() method. So
 * just create yourself a Yin, then call getPitch(bytes) when you're ready.
 *
 * Also converted it to use an array of Shorts instead of Floats.
 *
 * Original implementation is here: http://tarsos.0110.be/artikels/lees/YIN_Pitch_Tracker_in_JAVA
 */
class Yin //		bufferSize = 1024;
//		overlapSize = bufferSize/2;//half of the buffer overlaps
//		running = true;
//		inputBuffer = new short[bufferSize];
//		yinBuffer = new float[bufferSize/2];
    (//	private int bufferSize;
    //	private int overlapSize;
    private val sampleRate: Float
) {
    /**
     * Used to start and stop real time annotations.
     */
    //	private static Yin yinInstance;
    /**
     * The YIN threshold value (see paper)
     */
    private val threshold = 0.15

    /**
     * A boolean to start and stop the algorithm.
     * Practical for real time processing of data.
     */
    //	private volatile boolean running;
    /**
     * The original input buffer
     */
    private lateinit var inputBuffer: ShortArray

    /**
     * The buffer that stores the calculated values.
     * It is exactly half the size of the input buffer.
     */
    private lateinit var yinBuffer: FloatArray

    /**
     * Implements the difference function as described
     * in step 2 of the YIN paper
     */
    private fun difference() {
        var j: Int
        var delta: Float
        var tau = 0
        while (tau < yinBuffer.size) {
            yinBuffer[tau] = 0f
            tau++
        }
        tau = 1
        while (tau < yinBuffer.size) {
            j = 0
            while (j < yinBuffer.size) {
                delta = inputBuffer[j].toFloat() - inputBuffer[j + tau].toFloat()
                yinBuffer[tau] += delta * delta
                j++
            }
            tau++
        }
    }

    /**
     * The cumulative mean normalized difference function
     * as described in step 3 of the YIN paper
     * <br></br>`
     * yinBuffer[0] == yinBuffer[1] = 1
    ` *
     *
     */
    private fun cumulativeMeanNormalizedDifference() {
        yinBuffer[0] = 1f
        //Very small optimization in comparison with AUBIO
        //start the running sum with the correct value:
        //the first value of the yinBuffer
        var runningSum = yinBuffer[1]
        //yinBuffer[1] is always 1
        yinBuffer[1] = 1f
        //now start at tau = 2
        var tau = 2
        while (tau < yinBuffer.size) {
            runningSum += yinBuffer[tau]
            yinBuffer[tau] *= tau / runningSum
            tau++
        }
    }

    /**
     * Implements step 4 of the YIN paper
     */
    private fun absoluteThreshold(): Int {
        //Uses another loop construct
        //than the AUBIO implementation
        var tau = 1
        while (tau < yinBuffer.size) {
            if (yinBuffer[tau] < threshold) {
                while (tau + 1 < yinBuffer.size &&
                    yinBuffer[tau + 1] < yinBuffer[tau]
                ) tau++
                return tau
            }
            tau++
        }
        //no pitch found
        return -1
    }

    /**
     * Implements step 5 of the YIN paper. It refines the estimated tau value
     * using parabolic interpolation. This is needed to detect higher
     * frequencies more precisely.
     *
     * @param tauEstimate
     * the estimated tau value.
     * @return a better, more precise tau value.
     */
    private fun parabolicInterpolation(tauEstimate: Int): Float {
        val s0: Float
        val s2: Float
        val x0 = if ((tauEstimate < 1)) tauEstimate else tauEstimate - 1
        val x2 = if ((tauEstimate + 1 < yinBuffer.size)) tauEstimate + 1 else tauEstimate
        if (x0 == tauEstimate) return if ((yinBuffer[tauEstimate] <= yinBuffer[x2])) tauEstimate.toFloat() else x2.toFloat()
        if (x2 == tauEstimate) return if ((yinBuffer[tauEstimate] <= yinBuffer[x0])) tauEstimate.toFloat() else x0.toFloat()
        s0 = yinBuffer[x0]
        val s1 = yinBuffer[tauEstimate]
        s2 = yinBuffer[x2]
        //fixed AUBIO implementation, thanks to Karl Helgason:
        //(2.0f * s1 - s2 - s0) was incorrectly multiplied with -1
        return tauEstimate + 0.5f * (s2 - s0) / (2.0f * s1 - s2 - s0)
    }

    /**
     * The main flow of the YIN algorithm. Returns a pitch value in Hz or -1 if
     * no pitch is detected using the current values of the input buffer.
     *
     * @return a pitch value in Hz or -1 if no pitch is detected.
     */
    fun getPitch(aInputBuffer: ShortArray): Float {
        inputBuffer = aInputBuffer
        yinBuffer = FloatArray(inputBuffer.size / 2)

        var tauEstimate = -1
        var pitchInHertz = -1f

        //step 2
        difference()

        //step 3
        cumulativeMeanNormalizedDifference()

        //step 4
        tauEstimate = absoluteThreshold()

        //step 5
        if (tauEstimate != -1) {
            val betterTau = parabolicInterpolation(tauEstimate)

            //step 6
            //TODO Implement optimization for the YIN algorithm.
            //0.77% => 0.5% error rate,
            //using the data of the YIN paper
            //bestLocalEstimate()

            //conversion to Hz
            pitchInHertz = sampleRate / betterTau
        }

        return pitchInHertz
    } //
    //	/**
    //	 * The interface to use to react to detected pitches.
    //	 * @author Joren Six
    //	 *
    //	 */
    //	public interface DetectedPitchHandler{
    //		/**
    //		 * Use this method to react to detected pitches.
    //		 * The handleDetectedPitch is called for every sample even when
    //		 * there is no pitch detected: in that case -1 is the pitch value.
    //		 * @param time in seconds
    //		 * @param pitch in Hz
    //		 */
    //		void handleDetectedPitch(float time,float pitch);
    //	}
    //
    //	/**
    //	 * Annotate a file with pitch information.
    //	 *
    //	 * @param fileName
    //	 *            the file to annotate.
    //	 * @param detectedPitchHandler
    //	 *            handles the pitch information.
    //	 * @throws UnsupportedAudioFileException
    //	 *             Currently only WAVE files with one channel (MONO) are
    //	 *             supported.
    //	 * @throws IOException
    //	 *             If there is an error reading the file.
    //	 */
    //
    //
    //	public static void processFile(String fileName,DetectedPitchHandler detectedPitchHandler)  throws UnsupportedAudioFileException, IOException{
    //		AudioInputStream ais = AudioSystem.getAudioInputStream(new File(fileName));
    //		AudioFloatInputStream afis = AudioFloatInputStream.getInputStream(ais);
    //		Yin.processStream(afis,detectedPitchHandler);
    //	}
    //
    //	/**
    //	 * Annotate an audio stream: useful for real-time pitch tracking.
    //	 *
    //	 * @param afis
    //	 *            The audio stream.
    //	 * @param detectedPitchHandler
    //	 *            Handles the pitch information. If null then the annotated
    //	 *            pitch information is printed to <code>System.out</code>
    //	 * @throws UnsupportedAudioFileException
    //	 *             Currently only WAVE streams with one channel (MONO) are
    //	 *             supported.
    //	 * @throws IOException
    //	 *             If there is an error reading the stream.
    //	 */
    //	public static void processStream(AudioFloatInputStream afis,DetectedPitchHandler detectedPitchHandler) throws UnsupportedAudioFileException, IOException{
    //		AudioFormat format = afis.getFormat();
    //		float sampleRate = format.getSampleRate();
    //		double frameSize = format.getFrameSize();
    //		double frameRate = format.getFrameRate();
    //		float time = 0;
    //
    //		//by default use the print pitch handler
    //		if(detectedPitchHandler==null)
    //			detectedPitchHandler = Yin.PRINT_DETECTED_PITCH_HANDLER;
    //
    //		//number of bytes / frameSize * frameRate gives the number of seconds
    //		//because we use float buffers there is a factor 2: 2 bytes per float?
    //		//Seems to be correct but a float uses 4 bytes: confused programmer is confused.
    //		float timeCalculationDivider = (float) (frameSize * frameRate / 2);
    //		long floatsProcessed = 0;
    //		yinInstance = new Yin(sampleRate);
    //		int bufferStepSize = yinInstance.bufferSize - yinInstance.overlapSize;
    //
    //		//read full buffer
    //		boolean hasMoreBytes = afis.read(yinInstance.inputBuffer,0, yinInstance.bufferSize) != -1;
    //		floatsProcessed += yinInstance.inputBuffer.length;
    //		while(hasMoreBytes && yinInstance.running) {
    //			float pitch = yinInstance.getPitch();
    //			time = floatsProcessed / timeCalculationDivider;
    //			detectedPitchHandler.handleDetectedPitch(time,pitch);
    //			//slide buffer with predefined overlap
    //			for(int i = 0 ; i < bufferStepSize ; i++)
    //				yinInstance.inputBuffer[i]=yinInstance.inputBuffer[i+yinInstance.overlapSize];
    //			hasMoreBytes = afis.read(yinInstance.inputBuffer,yinInstance.overlapSize,bufferStepSize) != -1;
    //			floatsProcessed += bufferStepSize;
    //		}
    //	}
    //
    //	/**
    //	 * Stops real time annotation.
    //	 */
    //	public static void stop(){
    //		if(yinInstance!=null)
    //			yinInstance.running = false;
    //	}
    //
    //
    //	public static DetectedPitchHandler PRINT_DETECTED_PITCH_HANDLER = new DetectedPitchHandler() {
    //		@Override
    //		public void handleDetectedPitch(float time, float pitch) {
    //			System.out.println(time + "\t" + pitch);
    //		}
    //	};
    //
    //	public static void main(String... args) throws UnsupportedAudioFileException, IOException{
    //		Yin.processFile("../Tarsos/audio/pitch_check/brass-880.wav", PRINT_DETECTED_PITCH_HANDLER);
    //	}
}
