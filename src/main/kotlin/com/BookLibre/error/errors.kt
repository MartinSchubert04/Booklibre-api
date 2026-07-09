package com.BookLibre.error

import java.lang.RuntimeException

class BusinessException(msg: String) : RuntimeException(msg)

class NotFoundException(msg: String) : RuntimeException(msg)