package com.example.minesweeper;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {
    static BlockButton[][] buttons;
    static TableLayout table;
    static TextView textView;
    static ToggleButton toggle1,toggle2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        table = (TableLayout) findViewById(R.id.tableLayout);
        textView = (TextView)findViewById(R.id.textView);

        gameStart();    //게임 시작

        Button restartButton = (Button)findViewById(R.id.restartButton);
        restartButton.setOnClickListener(new View.OnClickListener() {   //재시작 버튼 눌렀을 때 
            @Override
            public void onClick(View view) {
                reStart();  //게임 재시작
            }
        });
        toggle1 = (ToggleButton) findViewById(R.id.toggleButton);
        toggle1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            //토글 버튼이 토글 될때마다
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean on) {
                //모든 블럭들한테 다시 클릭 리스너 세팅
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {
                        buttons[i][j].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //토글이 ON 일 때 깃발 꽂기, OFF 일 때 블럭 깨기
                                if (on) ((BlockButton) view).toggleFlag();
                                else    ((BlockButton) view).breakBlock();
                            }
                        });
                    }
                }
            }
        });
        toggle2 = (ToggleButton)findViewById(R.id.toggleBotton2);
        toggle2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean on) {
                if(on) toggle1.setEnabled(false);
                else   toggle1.setEnabled(true);
                for (int i = 0; i < 9; i++) {
                    for (int j = 0; j < 9; j++) {
                        buttons[i][j].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                //토글이 ON 일 때 블럭 자동 깨기, OFF 일 때 원래대로
                                if (on) ((BlockButton) view).autoBreak();
                                else{
                                    if(toggle1.isChecked()) ((BlockButton) view).toggleFlag();
                                    else                    ((BlockButton) view).breakBlock();
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    private void gameStart(){   //게임 시작 함수
        buttons = new BlockButton[9][9];    //9*9 BlockButton 배열 생성
        BlockButton.flags=0;    //처음 꽂힌 깃발 0개
        BlockButton.blocks=0;   //재시작 했을 때 블럭개수 다시 세야해서
        for(int i=0;i<9;i++){   //테이블에 9개 행 삽입
            TableRow tableRow = new TableRow(this);
            table.addView(tableRow);
            for(int j=0;j<9;j++){   //각 테이블 열에 버튼 9개 삽입
                buttons[i][j] = new BlockButton(this,i,j);
                tableRow.addView(buttons[i][j]);
                TableRow.LayoutParams layoutParams =
                        new TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT,
                                1.0f);
                buttons[i][j].setLayoutParams(layoutParams);
                buttons[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((BlockButton)view).breakBlock();
                    }
                });
                BlockButton.blocks++;   //생성 될 때 block 개수 추가
            }
        }
        createMines();  //지뢰 생성
        //textView 표시
        textView.setText("Flags: "+Integer.toString(10-BlockButton.flags));
    }
    private void createMines() {    // 지뢰 랜덤 생성 함수
        int mines=0;
        while(mines < 10){
            int x = (int)(Math.random()*9); //0~8 랜덤
            int y = (int)(Math.random()*9); //0~8 랜덤
            if(!buttons[x][y].mine) {   //mine 중복 생성 방지
                buttons[x][y].mine = true;
                mines++;
            }
        }
    }
    private void reStart() {    //게임 재시작 함수
        table.removeAllViews(); //table 안에 모든 TableRaw, Button 삭제
        toggle1.setChecked(false);
        toggle2.setChecked(false);
        gameStart();    //게임 시작
    }
    private static void gameEnd(String icon, int color, String message) {  //게임 끝났을 때
        //지뢰 있는 곳 표시
        for(int i=0;i<9;i++){
            for(int j=0;j<9;j++){
                BlockButton b = MainActivity.buttons[i][j];
                if(b.mine){
                    b.setTypeface(Typeface.DEFAULT_BOLD);
                    b.setText(icon);    //모양 표시
                    b.setTextColor(color);  //색깔 변경
                }
                b.setEnabled(false);    //모든 블럭 비활성화
            }
        }
        //토스트 메세지 출력
        Toast.makeText(textView.getContext(), message,Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("AppCompatCustomView")
    static class BlockButton extends Button{
        int x,y;    //좌표
        boolean mine,flag,isBroken; //지뢰가 있는지, 깃발 꽂혔는지, 이미 깬 블럭인지
        int neighborMines;  //주변에 있는 지뢰,깃발 개수
        static int flags;   //flagged blocks
        static int blocks;  //remaining blocks

        public BlockButton(Context context, int x, int y) { //생성자
            super(context);
            this.x = x; this.y = y;
            setScaleX(1.1f);
            setScaleY(1.2f);
        }
        public void toggleFlag(){   //깃발 꽂기 함수
            if(!isBroken) { //안깨져있는 블록에만
                if (flag) { //깃발이 이미 꽂혀있으면
                    flags--;
                    flag = false;
                    setText(null);
                } else {    //깃발이 안 꽂혀있으면
                    flags++;
                    flag = true;
                    setText("P");
                    setTypeface(Typeface.DEFAULT_BOLD);
                    setTextColor(Color.RED);
                }
                //깃발 개수 변동될 때 마다 업데이트
                textView.setText("Flags: " + (Integer.toString(10 - flags)));
            }
        }
        public void breakBlock(){
            if(!isBroken) {  //안깨져있는 블록에만
                if (!flag) { //깃발이 안꽂혀 있어야만 실행
                    if (mine) { //지뢰 밟은경우(패배)
                        gameEnd("※", Color.BLACK, "Game Over...");
                    } else {  //지뢰 아닌경우
                        displayMine();  //지뢰 개수 표시
                        if(neighborMines==0) {  //지뢰가 주변에 없으면
                            toNeighbors("BreakBlocks"); //주변 블럭 다 깨기
                        }
                        if (blocks == 10) {  //10개 남기고 다 깨면(승리)
                            gameEnd("○", Color.MAGENTA, "Congratulation!");
                        }
                    }
                }
            }
        }
        private void displayMine() {    //지뢰 개수 표시함수
            neighborMines = toNeighbors("CountMines");   //주변 지뢰 개수 계산
            setBackgroundColor(Color.GRAY);
            setScaleX(0.95f);
            setScaleY(0.95f);
            isBroken = true;
            blocks--;
            if (neighborMines != 0) {   //주변에 지뢰가 있으면(숫자 블럭이면)
                setText(Integer.toString(neighborMines));
                switch (neighborMines) {  //숫자에 따른 색깔 설정
                    case 1: setTextColor(Color.rgb(100, 255, 100)); break;
                    case 2: setTextColor(Color.BLUE);   break;
                    case 3: setTextColor(Color.rgb(100, 0, 150));   break;
                    case 4: setTextColor(Color.YELLOW); break;
                    default: setTextColor(Color.RED);   break;
                    }
            } else {  //주변에 지뢰가 없으면
                setEnabled(false);
                setText(null);
            }
        }
        public void autoBreak() {   //블럭 자동 깨기 함수
            if(isBroken && neighborMines == toNeighbors("CountFlags")){
                //숫자 블럭 중에 주변 지뢰 개수와 주변 깃발 개수가 같으면
                toNeighbors("AutoBreak"); //주변 블럭 다 깨기
            }
        }
        private int toNeighbors(String str){    //주변 블럭대상 함수
            int count=0;
            for(int i=-1 ; i<=1 ; i++){
                for(int j= -1 ; j<=1 ; j++){
                    if (x+i >= 0 && x+i <= 8 && y+j >= 0 && y+j <= 8) {
                        BlockButton b = buttons[x+i][y+j];
                        switch (str) {
                            case "BreakBlocks": if(b.flag)  b.toggleFlag();
                            case "AutoBreak":   b.breakBlock(); break;
                            case "CountMines":  //지뢰 개수 세기
                                if (b.mine) count++;
                                break;
                            case "CountFlags":  //깃발 개수 세기
                                if(b.flag)  count++;
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
            return count;
        }
    }
}
