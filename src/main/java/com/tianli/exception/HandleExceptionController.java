package com.tianli.exception;

import cn.hutool.json.JSONUtil;
import com.tianli.common.async.AsyncService;
import com.tianli.common.webhook.WebHookService;
import com.tianli.tool.ApplicationContextTool;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wangqiyun on 2018/1/18.
 */
@RestControllerAdvice
public class HandleExceptionController {
    @ExceptionHandler(value = {Exception.class})
    public Result resolveException(HttpServletRequest request, Exception e) {
        if (!(e instanceof ErrCodeException)
                && !(e instanceof HttpRequestMethodNotSupportedException)
                && !(e instanceof MissingRequestHeaderException)) {
            var url = request.getRequestURL().toString();
            Map<String, String[]> parameterMap = request.getParameterMap();
            url = url + "  params:" + JSONUtil.toJsonStr(parameterMap);
            webHookService.dingTalkSend("异常信息", url, e);
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
        if (!(e instanceof ErrCodeException) ||
                ErrorCodeEnum.UNLOIGN.getErrorNo() != Integer.parseInt(((ErrCodeException) e).errcode)) {
            e.printStackTrace();
        }
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


    @ExceptionHandler(value = CustomException.class)
    public Result customExceptionHandler(CustomException e) {
        Result result = Result.instance();
        String message = e.getMessage();
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher matcher = p.matcher(message);
        String trim = matcher.replaceAll("").trim();
        String[] num = {trim};
        String s = num[0];
        int firstChinaChartIndex = getFirstChinaChartIndex(message);
        String substring = message.substring(firstChinaChartIndex, message.indexOf(s));
        String transMsg = getEnMsg(substring);
        result.setCode(e.getCode().toString());
        result.setMsg(e.getMessage());
        result.setEnMsg(message.substring(0, firstChinaChartIndex) + " " + transMsg + "" + message.substring(message.indexOf(s)));
        return result;
    }

    private String getEnMsg(String msg) {
        String transMsg = errMsgMappingService.getTransMsg("en", msg);
        return transMsg == null ? msg : transMsg;
    }

    private String getThaiMsg(String msg) {
        String transMsg = errMsgMappingService.getTransMsg("thai", msg);
        return transMsg == null ? msg : transMsg;
    }

    /**
     * 获取第一个中文字符索引
     *
     * @param str
     * @return
     */
    private int getFirstChinaChartIndex(String str) {
        int beginIndex = 0;
        for (int index = 0; index <= str.length() - 1; index++) {
            //将字符串拆开成单个的字符
            String w = str.substring(index, index + 1);
            if (w.compareTo("\u4e00") > 0 && w.compareTo("\u9fa5") < 0) {// \u4e00-\u9fa5 中文汉字的范围
                beginIndex = index;
                break;
            }
        }
        return beginIndex;
    }

    private static boolean checkIfExistChineseCharacter(String s) {
        return !(s.length() == s.getBytes().length);
    }

    @Resource
    private ErrMsgMappingService errMsgMappingService;
    @Resource
    private WebHookService webHookService;

}
