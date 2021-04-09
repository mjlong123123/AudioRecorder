#ifndef _AUTOLOCK_H
#define _AUTOLOCK_H
#include "jmutex.h"
class Autolock
{
public:
    //when construct object lock
    inline Autolock(JMutex & mutex) : mLock(mutex)
    {
        if(!mLock.IsInitialized())
        {
            if(mLock.Init() < 0)
            {
                return;
            }
        }
        mLock.Lock();
    }
    //when release object unlock
    inline ~Autolock()
    {
        mLock.Unlock();
    }
private:
    JMutex & mLock;
};

#endif