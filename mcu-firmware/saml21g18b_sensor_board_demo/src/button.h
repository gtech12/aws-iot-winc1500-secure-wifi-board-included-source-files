/*
 * Support and FAQ: visit <a href="http://www.atmel.com/design-support/">Atmel
 * Support</a>
 */

#ifndef __BUTTON_H__
#define __BUTTON_H__

#define TIMEOUT_COUNTER_BUTTON_DEBOUNCE		(50)		//200ms
#define TIMEOUT_COUNTER_5S		(5*1000)		//5seconds
#define MAX_CB_INDEX			3
#define NUMBER_OF_BUTTON		3
//typedef void *(button_5s_timeout_cb[MAX_CB_INDEX])();
void (*button_long_press_cb[NUMBER_OF_BUTTON][MAX_CB_INDEX]) (void);
void (*button_short_press_cb[NUMBER_OF_BUTTON][MAX_CB_INDEX]) (void);
//const button_5s_timeout_cb **button_5s_timeout_cb_ptr = NULL;
//const button_5s_timeout_cb *button_5s_timeout_cb_ptr[MAX_CB_INDEX];

void initialise_button(void);
void buttonInitCheck(void);
void buttonTaskInit(void);
void buttonTaskExecute(uint32_t tick);
int regButtonShortPressDetectCallback(void* cb, int button);	// Argument Button is 1,2 or 3
int unRegShortButtonPressDetectCallback(int sock, int button);
int regButtonLongPressDetectCallback(void* cb, int button);
int unRegButtonLongPressDetectCallback(int sock, int button);


#endif /*__BUTTON_H__*/
