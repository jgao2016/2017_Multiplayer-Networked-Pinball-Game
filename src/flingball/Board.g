@skip whitespaceAndComments {
board::='board name=' NAME ('gravity' '='GRAVITY)? ('friction1' '='FRICTION1)? ('friction2' '='FRICTION2)? gadget*;
ball::='ball' 'name' '=' NAME 'x' '=' FLOAT 'y' '=' FLOAT 'xVelocity' '=' FLOAT 'yVelocity' '=' FLOAT;
squareBumper::='squareBumper' 'name' '=' NAME 'x' '=' INTEGER 'y' '=' INTEGER ;
circleBumper::='circleBumper' 'name' '=' NAME 'x' '=' INTEGER 'y' '=' INTEGER;
triangleBumper::='triangleBumper' 'name' '=' NAME 'x' '=' INTEGER 'y' '=' INTEGER ('orientation' '=' ORIENTATION)?;
absorber::= 'absorber' 'name' '=' NAME 'x' '=' INTEGER 'y' '=' INTEGER 'width' '='INTEGER 'height' '='INTEGER;
rightFlipper::='rightFlipper' 'name''=' NAME 'x''=' INTEGER 'y''=' INTEGER ('orientation''=' ORIENTATION)?;
leftFlipper::='leftFlipper' 'name''=' NAME 'x''=' INTEGER 'y''=' INTEGER ('orientation''=' ORIENTATION)?;
portal::='portal' 'name''=' NAME 'x''=' INTEGER 'y''=' INTEGER ('otherBoard''=' BOARDNAME)? 'otherPortal''=' NAME;
fire::='fire' 'trigger' '=' TRIGGER 'action' '='ACTION;
keydown::='keydown' 'key' '=' KEY 'action' '=' ACTION;
keyup::='keyup' 'key' '=' KEY 'action' '=' ACTION;
gadget::= ball|squareBumper|circleBumper|triangleBumper|absorber|fire|keydown|keyup|portal|rightFlipper|leftFlipper;
}
GRAVITY::=FLOAT;
FRICTION1::=FLOAT;
FRICTION2::=FLOAT;
TRIGGER::=NAME;
ACTION::=NAME;
BOARDNAME::=NAME;

ORIENTATION::='0'|'90'|'180'|'270';
INTEGER::=[0-9]+;
FLOAT::= '-'?([0-9]+'.'?[0-9]*|'.'[0-9]+);
NAME::=[A-Za-z_][A-Za-z_0-9]*;
          
KEY ::= [a-z] | [0-9] | 'shift' | 'ctrl' | 'alt' | 'meta'| 'space' | 'left' | 'right' 
        | 'up' | 'down'| 'minus' | 'equals' | 'backspace'| 'openbracket' | 'closebracket' 
        | 'backslash'| 'semicolon' | 'quote' | 'enter'| 'comma' | 'period' | 'slash';
        
whitespaceAndComments::=whitespace|comments;
whitespace::=[ \t\r\n]+;
comments::='#'+[^\r\n]*[\r\n]+;