package com.tianli.wallet.enums;

import lombok.Getter;

/**
 * @author chenb
 * @apiNote 钱包状态
 * @since 2022-07-06
 **/
@Getter
public enum AccountActiveStatus {

    ACTIVE((byte) 0);

    AccountActiveStatus(byte type){
        this.type = type;
    }
    private final byte type;


    public static AccountActiveStatus getByType(byte type){
      for(AccountActiveStatus walletActiveStatus : AccountActiveStatus.values())  {
          if(walletActiveStatus.type == type){
              return walletActiveStatus;
          }
      }
      return null;
    }
}
