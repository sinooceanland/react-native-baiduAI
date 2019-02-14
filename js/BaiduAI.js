/**
 功能介绍 人脸采集
 作者：GJ
 Date：2019/02/14
 */
import React,{
    Component,
    PropTypes
} from 'react';
import ReactNative,{
    findNodeHandle,
    NativeModules,
    Platform,
    requireNativeComponent,
    UIManager, View
} from 'react-native'

var DetectionView = requireNativeComponent('DetectionView', null);

export default class BaiduAIView extends Component{

    static propTypes = {
        ...View.propTypes,

        style: View.propTypes.style,
        /**
         * 扫码结果的回调
         */
        onRecongnizeFinish: PropTypes.func
    }
    constructor(props){
        super(props)

    }
    render(){
        return(
            <DetectionView
                {...this.props}
                ref="DetectionView"
            />
        )
    }
    /*重新采集人像
    * */
    reload = () => {
        UIManager.dispatchViewManagerCommand(
            this.getViewHandle(),
            UIManager.DetectionView.Commands.reload,
            [],
        )
    }
    /*start采集服务的线程
    * */
    start = () =>{
        //android需要控制服务的开闭，iOS不需要特别控制
        if (Platform.OS == 'ios'){

        } else {
            UIManager.dispatchViewManagerCommand(
                this.getViewHandle(),
                UIManager.DetectionView.Commands.start,
                [],
            )
        }
    }
    /*销毁采集服务线程
    * */
    destroy = () =>{
        //android需要控制服务的开闭，iOS不需要特别控制
        if (Platform.OS == 'ios'){

        } else {
            UIManager.dispatchViewManagerCommand(
                this.getViewHandle(),
                UIManager.DetectionView.Commands.destroy,
                [],
            )
        }
    }
    getViewHandle() {
        return ReactNative.findNodeHandle(this.refs["DetectionView"]);
    }
}
