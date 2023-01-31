package com.tianli.management.controller;

import com.tianli.exception.Result;
import com.tianli.management.query.FinancialBoardQuery;
import com.tianli.management.query.TimeQuery;
import com.tianli.management.service.FinancialBoardProductService;
import com.tianli.management.service.FinancialBoardWalletService;
import com.tianli.management.service.ServiceFeeService;
import com.tianli.management.vo.BoardAssetsVO;
import com.tianli.management.vo.BoardFinancialVO;
import com.tianli.management.vo.BoardServiceFeeVO;
import com.tianli.management.vo.BoardWalletVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author chenb
 * @apiNote
 * @since 2023-01-31
 **/
@RestController
@RequestMapping("/management/board/")
public class ManageBoardController {

    @Resource
    private ServiceFeeService serviceFeeService;
    @Resource
    private FinancialBoardProductService financialProductBoardService;
    @Resource
    private FinancialBoardWalletService financialWalletBoardService;

    /**
     * 提现手续费展板
     */
    @GetMapping("/serviceFee/board")
    public Result<BoardServiceFeeVO> serviceFee(TimeQuery timeQuery, Byte type) {
        return new Result<>(serviceFeeService.board(timeQuery, type));
    }

    /**
     * 数据展板
     */
    @GetMapping("/financial")
    public Result<BoardFinancialVO> financial(FinancialBoardQuery query) {
        query.calTime();
        return new Result<>(financialProductBoardService.productBoard(query));
    }

    /**
     * 【云钱包数据展板】
     */
    @GetMapping("/wallet")
    public Result<BoardWalletVO> board(FinancialBoardQuery query) {
        query.calTime();
        return new Result<>(financialWalletBoardService.walletBoard(query));
    }

    /**
     * 【云钱包数据展板】
     */
    @GetMapping("/assets")
    public Result<BoardAssetsVO> hotWallet(FinancialBoardQuery query) {
        query.calTime();
        return new Result<>(financialWalletBoardService.assetsBoard(query));
    }


}
