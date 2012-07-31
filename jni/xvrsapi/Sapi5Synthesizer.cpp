#include "com_avian_xvr_media_sapi_Sapi5Synthesizer.h"
#include <windows.h>
#include <comcat.h>
#include <atlbase.h>
#include <sapi.h>

//#include <sphelper.h>


JNIEXPORT jbyteArray JNICALL Java_com_avian_xvr_media_sapi_Sapi5Synthesizer_synthToBuffer
  (JNIEnv *, jobject, jstring)
{
	HRESULT				hr = S_OK;
	CComPtr <ISpVoice>		cpVoice;
	CComPtr <ISpStream>		cpStream;
	CComPtr <ISpObjectToken>	cpToken;
	CComPtr <IEnumSpObjectTokens>	cpEnum;

//	CSpStreamFormat			cAudioFmt;

	//Enumerate voice tokens with attribute "Name=Microsoft Sam” 
	/*
	if(SUCCEEDED(hr))
	{
		hr = SpEnumTokens(SPCAT_VOICES, L"Name=Cepstrum David", NULL, &cpEnum);
	}
    */
	//Get the closest token
	if(SUCCEEDED(hr))
	{
		hr = cpEnum ->Next(1, &cpToken, NULL);
	}

	//Create a SAPI Voice
	hr = cpVoice.CoCreateInstance( CLSID_SpVoice );

	//Set the audio format 
	/*
    if(SUCCEEDED(hr))
	{
		hr = cAudioFmt.AssignFormat(SPSF_8kHz16BitMono);
	}
	*/
	
	//Call SPBindToFile, a SAPI helper method,  to bind the audio stream to the file
	if(SUCCEEDED(hr))
	{

//		hr = SPBindToFile( L”c:\\ttstemp.wav”,  SPFM_CREATE_ALWAYS, 
//			&cpStream, & cAudioFmt.FormatId(),cAudioFmt.WaveFormatExPtr() );
	}
	
	//set the output to cpStream so that the output audio data will be stored in cpStream
        if(SUCCEEDED(hr))
	{
		hr = cpVoice->SetOutput( cpStream, TRUE );
	}

 	//Speak the text “hello world” synchronously
        if(SUCCEEDED(hr))
	{
		hr = cpVoice->Speak( L"Hello World",  SPF_DEFAULT, NULL );
	}
	
	//close the stream
	if(SUCCEEDED(hr))
	{
		hr = cpStream->Close();
	}

	//Release the stream and voice object
	cpStream.Release ();
	cpVoice.Release();

	return NULL;
}
