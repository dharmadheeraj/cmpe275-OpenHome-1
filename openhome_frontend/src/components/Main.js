import React, {Component} from 'react';
import {Route} from 'react-router-dom';
import Home from './Home/Home';
import Properties from './Properties/Properties';
import Booking from './Booking/Booking';
import SignUp from './signup/signup.js';
import Login from './login/login.js';
import VerifyAccount from './signup/verifyaccount';

class Main extends Component {
    render(){
        return(
            <div>
                <Route path="/home" component={Home}/>
                <Route path="/properties" component={Properties}/>
                <Route path="/booking" component={Booking}/>
                <Route  exact path="/signup" render ={() => (<SignUp/>)}/>
		        <Route  exact path="/login" render ={() => (<Login/>)}/>
		        <Route  path="/verifyaccount/:ID" render ={(match) => (<VerifyAccount {...match} />)} />
            </div>
        )
    }
}
//Export The Main Component
export default Main;