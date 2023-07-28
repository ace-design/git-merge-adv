from typing import Union
class SchemaError(Exception):
    def __init__(self, detail: Union[str, dict]) ->None:
        self.detail = detail
        super().__init__(detail)
    

class ConfigurationError(Exception):

class APIException(Exception):
    default_status_code = 500
    default_message = 'Server error'
    default_detail = 'Server error'
    def __init__(self, detail: Union[str, dict]=None, status_code: int=None
        ) ->None:
        self.detail = self.default_detail if detail is None else detail
        self.status_code = (self.default_status_code if status_code is None else
            status_code)
    

class ValidationError(APIException):
    default_status_code = 400
    default_message = 'Invalid request'
    default_detail = 'Invalid request'

class NotFound(APIException):
    default_status_code = 404
    default_message = 'Not found'
    default_detail = 'Not found'

class MethodNotAllowed(APIException):
    default_status_code = 405
    default_message = 'Method not allowed'
    default_detail = 'Method not allowed'

class Found(APIException):
    default_status_code = 302
    default_message = 'Found'
    def __init__(self, location):
        self.location = location
        super().__init__()
    

