package com.avian.xvr.media.sphinx;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import com.avian.iaf.filters.IFilter;
import com.avian.iaf.filters.IInputPin;
import com.avian.iaf.filters.IOutputPin;
import com.avian.iaf.filters.ISinkFilter;
import com.avian.iaf.filters.MemoryInputPin;

import edu.cmu.sphinx.frontend.BaseDataProcessor;
import edu.cmu.sphinx.frontend.Data;
import edu.cmu.sphinx.frontend.DataProcessingException;
import edu.cmu.sphinx.frontend.DataStartSignal;
import edu.cmu.sphinx.frontend.DoubleData;

/**
 * This filter takes audio data off the graph, buffers it, and then
 * instructs Sphinx to retrieve it and insert it into its own pipeline.
 * 
 * @author Zack
 *
 */
public class SphinxRecognizerFilter extends BaseDataProcessor implements ISinkFilter  {

	public IInputPin inPin;
	
	/* (non-Javadoc)
	 * @see com.vogistix.iaf.filters.IFilter#getType()
	 */
	public int getType() {
		return IFilter.SINK;
	}
	
	public SphinxRecognizerFilter() {
		super();
		inPin = new MemoryInputPin(this);
		audioFifo = new DataList();
	}

	public boolean initialize(Properties p) {
		sampleCount = 0;
		/** signal to the recognizer that data is coming **/
		audioFifo.add(new DataStartSignal());
		return true;
	}

	public IOutputPin processInput(ByteBuffer src, ByteBuffer dest)
			throws UnsupportedOperationException {
		
		src.reset();
		
		DoubleBuffer doubleSrc = src.asDoubleBuffer();
		double[] doubleArray = new double[doubleSrc.remaining()];

		doubleSrc.get(doubleArray);
		
		DoubleData doubleData = new DoubleData(doubleArray, 8000, 
				System.currentTimeMillis(), sampleCount);
	
		sampleCount += doubleArray.length;
		
		audioFifo.add(doubleData);
		
		return null;
	}

	public void shutdown() {
	
	}

	@Override
	public Data getData() throws DataProcessingException {
		return audioFifo.remove();
	}
	
	DataList audioFifo;
	long sampleCount;
}

/**
 * Manages the data as a FIFO queue
 */
class DataList {

    private List<Data> list;

    /**
     * Creates a new data list
     */
    public DataList() {
        list = new LinkedList<Data>();
    }

    /**
     * Adds a data to the queue
     *
     * @param data the data to add
     */
    public synchronized void add(Data data) {
//		System.out.println("adding data...");
        list.add(data);
        notify();
    }

    /**
     * Returns the current size of the queue
     *
     * @return the size of the queue
     */
    public synchronized int size() {
        return list.size();
    }

    /**
     * Removes the oldest item on the queue
     *
     * @return the oldest item
     */
    public synchronized Data remove() {
        try {
            while (list.size() == 0) {
                // System.out.println("Waiting...");
                wait();
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
//		System.out.println("return data from datalist...");
        Data data = (Data) list.remove(0);
        if (data == null) {
            System.out.println("DataList is returning null.");
        }
        return data;
    }
}