package com.example.artcheckin;

import android.app.ActionBar.LayoutParams;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CustomDialog extends Dialog {
	
	public CustomDialog(Context context, int theme) {
        super(context, theme);
    }
  
    public CustomDialog(Context context) {
        super(context);
    }
    
    /**
     * Helper class for creating a custom dialog
     */
    public static class Builder {
  
		private Context context;
        private String title;
        private String message;
        private String positiveButtonText;
        private String negativeButtonText;
        private String neutralButtonText;
        private View contentView;
        private int iconResId = 0;
        private Button positiveBtn = null;
        private Button negativeBtn = null;
        private Button neutralBtn = null;
//        private CustomDialog dialog = null;
//    	private static boolean isDialogShowing = false;
  
        private DialogInterface.OnClickListener positiveButtonClickListener, negativeButtonClickListener, neutralButtonClickListener;
  
        public Builder(Context context) {
            this.context = context;    
//            dialog = new CustomDialog(context,R.style.infoDialogTheme);
        }
  
        /**
         * Set the Dialog message from String
         * @param title
         * @return
         */
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }
  
        /**
         * Set the Dialog message from resource
         * @param title
         * @return
         */
        public Builder setMessage(int message) {
            this.message = (String) context.getText(message);
            return this;
        }
  
        /**
         * Set the Dialog title from resource
         * @param title
         * @return
         */
        public Builder setTitle(int title) {
            this.title = (String) context.getText(title);
            return this;
        }
  
        /**
         * Set the Dialog title from String
         * @param title
         * @return
         */
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }
  
        /**
         * Set a custom content view for the Dialog.
         * If a message is set, the contentView is not
         * added to the Dialog...
         * @param v
         * @return
         */
        public Builder setContentView(View v) {
            this.contentView = v;
            return this;
        }
  
        /**
         * Set the positive button resource and it's listener
         * @param positiveButtonText
         * @param listener
         * @return
         */
        public Builder setPositiveButton(int positiveButtonText, DialogInterface.OnClickListener listener) {
            this.positiveButtonText = (String) context.getText(positiveButtonText);
            this.positiveButtonClickListener = listener;
            return this;
        }
  
        /**
         * Set the positive button text and it's listener
         * @param positiveButtonText
         * @param listener
         * @return
         */
        public Builder setPositiveButton(String positiveButtonText, DialogInterface.OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
            return this;
        }
  
        /**
         * Set the negative button resource and it's listener
         * @param negativeButtonText
         * @param listener
         * @return
         */
        public Builder setNegativeButton(int negativeButtonText, DialogInterface.OnClickListener listener) {
            this.negativeButtonText = (String) context.getText(negativeButtonText);
            this.negativeButtonClickListener = listener;
            return this;
        }
  
        /**
         * Set the negative button text and it's listener
         * @param negativeButtonText
         * @param listener
         * @return
         */
        public Builder setNegativeButton(String negativeButtonText, DialogInterface.OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;
            return this;
        }
        
        /**
         * Set the neutral button resource and it's listener
         * @param neutralButtonText
         * @param listener
         * @return
         */
        public Builder setNeutralButton(int neutralButtonText, DialogInterface.OnClickListener listener) {
            this.neutralButtonText = (String) context.getText(neutralButtonText);
            this.neutralButtonClickListener = listener;
            return this;
        }
  
        /**
         * Set the neutral button text and it's listener
         * @param neutralButtonText
         * @param listener
         * @return
         */
        public Builder setNeutralButton(String neutralButtonText, DialogInterface.OnClickListener listener) {
            this.neutralButtonText = neutralButtonText;
            this.neutralButtonClickListener = listener;
            return this;
        }
        
        public Builder setIcon(int resId){
        	iconResId = resId;
        	return this;
        }
        
        public View getButton(int whichButton){
        	switch (whichButton) {
        	case DialogInterface.BUTTON_POSITIVE: 
        		if(positiveButtonText != null) 
        			return positiveBtn;
        	case DialogInterface.BUTTON_NEGATIVE: 
        		if(negativeButtonText != null) 
        			return negativeBtn;
        	case DialogInterface.BUTTON_NEUTRAL: 
        		if(neutralButtonText != null) 
        			return neutralBtn;
        	default: return positiveBtn;
        	}
        }
        
//        public void showDialog(){
//        	System.out.println("----hyz----showDialog()---isDialogShowing--w---"+isDialogShowing);
//        	if (isDialogShowing == false){
//        		System.out.println("----hyz----showDialog()---isDialogShowing--l---"+isDialogShowing);
//        		dialog.show();
//        		isDialogShowing = true;
//        	}
//        }
        
//        public void dismissDialog(){
//        	System.out.println("----hyz----dismissDialog()---isDialogShowing---"+isDialogShowing);
//        		dialog.dismiss();
//        		isDialogShowing = false;
//        }
  
        /**
         * Create the custom dialog
         */
        public CustomDialog create() {
            // instantiate the dialog with the custom Theme
        	LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	final CustomDialog dialog = new CustomDialog(context,R.style.infoDialogTheme);
            View layout = inflater.inflate(R.layout.custom_dialog_layout, null);
            dialog.setCanceledOnTouchOutside(false);//点击屏幕其他的地方不能关闭dialog
            dialog.setCancelable(false);//点击返回键不能关闭dialog
            dialog.addContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            // set the dialog title
            if (title != null){
            	((TextView) layout.findViewById(R.id.title)).setText(title);
            }      
            // set the dialog icon
            if (iconResId != 0){
            	((ImageView) layout.findViewById(R.id.icon)).setImageResource(iconResId);
            }  
            // set the confirm button
            if (positiveButtonText != null) {
            	positiveBtn = (Button) layout.findViewById(R.id.positiveButton);
            	positiveBtn.setText(positiveButtonText);
                if (positiveButtonClickListener != null) {
                	positiveBtn.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    positiveButtonClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                                }
                            });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.positiveButton).setVisibility(View.GONE);
            }
             
            // set the cancel button
            if (negativeButtonText != null) {
                negativeBtn = (Button) layout.findViewById(R.id.negativeButton);
                negativeBtn.setText(negativeButtonText);
                if (negativeButtonClickListener != null) {
                	negativeBtn.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    negativeButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
                                }
                            });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.negativeButton).setVisibility(View.GONE);
            }
            
            if (neutralButtonText != null) {
                neutralBtn = (Button) layout.findViewById(R.id.neutralButton);
                neutralBtn.setText(neutralButtonText);
                if (neutralButtonClickListener != null) {
                	neutralBtn.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                	neutralButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEUTRAL);
                                }
                            });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.neutralButton).setVisibility(View.GONE);
            }
             
            // set the content message
            if (message != null) {
                ((TextView) layout.findViewById(R.id.message)).setText(message);
            } else if (contentView != null) {
                // if no message set
                // add the contentView to the dialog body
                ((LinearLayout) layout.findViewById(R.id.content)).removeAllViews();
                ((LinearLayout) layout.findViewById(R.id.content)).addView(contentView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            }
            dialog.setContentView(layout);
            return dialog;
        }
  
    }

}
