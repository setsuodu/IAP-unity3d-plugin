using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.EventSystems;
using UnityEngine.Events;
using UnityEngine.UI;

public class Example : MonoBehaviour //继承  
    , IPointerClickHandler //接口  
    , IPointerExitHandler
    , IPointerDownHandler
{
    public UnityEvent onLongPress = new UnityEvent();
    private float holdTime = 1f;
    public RectTransform menu;
    private Vector3 pos;
    public Transform selected;

    public void OnPointerClick(PointerEventData eventData)
    {
        //print("I was clicked:" + eventData.pointerCurrentRaycast.gameObject.name);  
    }

    public void OnPointerExit(PointerEventData eventData)
    {
        CancelInvoke("OnLongPress");
    }

    public void OnPointerDown(PointerEventData eventData)
    {
        selected = this.transform;
        pos = eventData.position;
        menu.pivot = new Vector2(eventData.position.x / Screen.width, eventData.position.y / Screen.height);
        menu.position = Vector3.zero;

        Invoke("OnLongPress", holdTime);
    }

    public void OnPointerUp(PointerEventData eventData)
    {
        selected = null;
        CancelInvoke("OnLongPress");
    }

    private void OnLongPress()
    {
        onLongPress.Invoke();
    }

    public void Haha()
    {
        //Debug.Log("Haha");
        menu.position = pos;
    }

    public void BackButton()
    {
        selected = null;
        menu.position = Vector3.zero;
    }

    public void OnCopy()
    {
        if(selected != null)
        Debug.Log("[]" + selected.GetComponentInChildren<Text>().text);
        //jo.Call("onClickCopy", m_clipInputField.text);
    }

    public void OnPaste()
    {
        //clipText.text = jo.Call<string>("onClickPaste");
    }
}