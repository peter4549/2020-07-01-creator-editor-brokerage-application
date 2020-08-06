package com.duke.elliot.kim.kotlin.creator_editor_brokerage_application

import okhttp3.Response

class ResponseFailedException(message: String?, val response: Response) : Exception(message)