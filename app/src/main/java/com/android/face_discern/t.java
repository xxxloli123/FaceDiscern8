package com.android.face_discern;

import android.content.res.Resources;

public class t {

    private void init(){
        if(paramUserInfoBean.getIsMaxLevel()==1){
            this.n.setText("����");
            this.o.setVisibility(4);
            localObject1 = this.x;
            localObject2 = new StringBuilder();
            ((StringBuilder) localObject2).append(paramUserInfoBean.getExceedPercent());
            ((StringBuilder) localObject2).append("%");
            ((TextView) localObject1).setText(((StringBuilder) localObject2).toString());
            this.x.setVisibility(0);
            this.p.setText("��������������");
        }else{
            this.x.setVisibility(8);
            this.o.setVisibility(0);
            if (paramUserInfoBean.getDailyViewNum() > 999) {
                this.n.setText("����");
            } else {
                this.n.setText(com.one.cucumber.c.a.a(2131493001, new Object[]{Integer.valueOf(paramUserInfoBean.getLeftViewNum()), Integer.valueOf(paramUserInfoBean.getDailyViewNum())}));
            }
        }

    }
    //                                      ...可变长度参数列表。
    public static String a(int paramInt, Object... paramVarArgs)
    {
        Resources localResources = a.getResources();
        if (localResources != null) {
            return localResources.getString(paramInt, paramVarArgs);
        }
        return "";
    }

    public void onClick(View paramAnonymousView)
    {
        if (System.currentTimeMillis() - MyCodeSharedActivity.a(MyCodeSharedActivity.this) < 2000L) {
            return;
        }
        MyCodeSharedActivity.a(MyCodeSharedActivity.this, System.currentTimeMillis());
        MyCodeSharedActivity.b(MyCodeSharedActivity.this);
        MyCodeSharedActivity.this.h();
        MyCodeSharedActivity.c(MyCodeSharedActivity.this).setTextColor(MyCodeSharedActivity.this.getResources().getColor(2131099850));
        MyCodeSharedActivity.c(MyCodeSharedActivity.this).setBackgroundResource(2131231131);
        MyCodeSharedActivity.c(MyCodeSharedActivity.this).setText("��������");
    }
}
