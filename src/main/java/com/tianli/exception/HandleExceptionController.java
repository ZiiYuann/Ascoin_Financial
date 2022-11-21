package com.tianli.exception;

import com.tianli.common.webhook.WebHookService;
import com.tianli.common.async.AsyncService;
import com.tianli.tool.ApplicationContextTool;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by wangqiyun on 2018/1/18.
 */
@RestControllerAdvice
public class HandleExceptionController {
    @ExceptionHandler(value = {Exception.class})
    public Result resolveException(HttpServletRequest request, Exception e) {
        if (!(e instanceof ErrCodeException)) {
            var url = request.getRequestURL().toString();
            webHookService.dingTalkSend("异常信息" + "【" + url + "】", e);
        }
        Result result = Result.instance();
        if (e instanceof ErrCodeException) {
            result.setCode(((ErrCodeException) e).getErrcode());
            String message = e.getMessage();
            result.setMsg(e.getMessage());
            result.setEnMsg(getEnMsg(message));
        } else if (Exceptions.getE().containsKey(e.getClass())) {
            result.setCode(Exceptions.getE().get(e.getClass()).getErrcode());
            String message = Exceptions.getE().get(e.getClass()).getMessage();
            result.setMsg(message);
            result.setEnMsg(getEnMsg(message));
        } else {
            result.setCode("100");
            result.setMsg("系统异常");
            result.setEnMsg("System exception");
        }
        ExceptionUtils.printStackTrace(e);
        e.printStackTrace();
        result.setTime(System.currentTimeMillis());
        AsyncService asyncService = ApplicationContextTool.getBean(AsyncService.class);
        if (asyncService != null)
            asyncService.cancel();
        return result;
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public Result paramExceptionHandler(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        if (bindingResult.hasErrors()) {
            List<ObjectError> errors = bindingResult.getAllErrors();
            if (!errors.isEmpty()) {
                FieldError fieldError = (FieldError) errors.get(0);
                Result result = Result.instance();
                result.setCode(ErrorCodeEnum.ARGUEMENT_ERROR.getErrorNo() + "");
                String defaultMessage = fieldError.getDefaultMessage();
                result.setMsg(defaultMessage);
                result.setEnMsg(getEnMsg(defaultMessage));
                return result;
            }
        }
        return Result.fail(ErrorCodeEnum.ARGUEMENT_ERROR);
    }

    private String getEnMsg(String msg) {
        String transMsg = errMsgMappingService.getTransMsg("en", msg);
        return transMsg == null ? msg : transMsg;
    }

    private String getThaiMsg(String msg) {
        String transMsg = errMsgMappingService.getTransMsg("thai", msg);
        return transMsg == null ? msg : transMsg;
    }

    @Resource
    private ErrMsgMappingService errMsgMappingService;
    @Resource
    private WebHookService webHookService;

}
